package com.ideaproject.etl.business.dataprovider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

/**
 * Provides and specifies functions to manipulate a generic data type sent to the Queue
 */
@Getter
@Slf4j
public abstract class DataProvider<T, D> {
    protected boolean isCanceled = false;
    @Value("${dispatcher.max.message.size:160000}")
    protected String maxMsgSize;
    /**
     * Gets the number of items
     *
     * @return number of items
     */
    public abstract long getItemsCount();

    /**
     * Gets all items in the pageable
     * @param pageable the current pageable
     * @return page containing items
     */
    protected abstract Page<T> getAllItems(Pageable pageable);

    /**
     * Builds a list of json payload to be sent via queue
     * @param page current page from the query result
     * @return the created json payload list
     */
    protected abstract List<String> buildTransferItems(Page<T> page);

    /**
     * Creates a transfer pojo based on the current list
     * @param list the current list
     * @return the created object to be sent via queue
     */
    protected abstract D createTransferDTO(List<T> list);

    /**
     * Starts reading data from the database
     *
     * @param consumeItem,   a consumer function to send item to the queue
     * @param pageSize,      size of pagination
     * @param postProcessor, a consumer function to log info after the data processing
     * @param <T>            current object type
     * @return true if started
     */
    public boolean startReading(Consumer<String> consumeItem, int pageSize, DataItemPostProcessor<String> postProcessor) {
        isCanceled = false;
        AtomicLong currentItemIndex = new AtomicLong();
        AtomicLong totalItemNumber = new AtomicLong();
        int currentPageNumber = -1;
        int totalPagesNumber = -1;

        DataItemPostProcessor<String> pp = postProcessor != null ? postProcessor : noActionPostProcessor;

        do {
            try {
                Pageable pageable = PageRequest.of(++currentPageNumber, pageSize);
                Page<T> items = getAllItems(pageable);
                if (nonNull(items)) {
                    totalItemNumber.set(items.getTotalElements());
                    totalPagesNumber = items.getTotalPages();
                    List<String> listItems = buildTransferItems(items);
                    long fromIdx = currentItemIndex.get();
                    long increment = Math.min(items.getNumberOfElements(), pageSize);
                    listItems.stream()
                            .takeWhile(x -> !isCanceled)
                                .forEachOrdered(consumeItem
                                    .andThen(x -> pp.postProcess(x,
                                            fromIdx,
                                            fromIdx + increment - 1,
                                            totalItemNumber.get())));
                    currentItemIndex.addAndGet(increment);
                }
            } catch (Exception e) {
                log.error("description={} cause={}", ExceptionUtils.getMessage(e), ExceptionUtils.getRootCauseMessage(e));
            }
        } while (!isCanceled && currentPageNumber < totalPagesNumber);

        return !isCanceled;
    }

    /**
     * Stops reading data from database
     *
     * @return true if the reading has been stopped
     */
    public boolean stopReading() {
        isCanceled = true;
        return isCanceled;
    }

    /**

     * Creates a json payload from the given object
     * @param payload object uses to create json payload
     * @return the created json payload
     * @throws JsonProcessingException,throws JsonProcessingException
     */
    protected String createJsonMessage(Object payload) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(payload);
    }


    private final DataItemPostProcessor<String> noActionPostProcessor = (item, from, to, size) -> {};

    /**
     * Creates list containing a list of result items
     * @param stream stream of the database's result
     * @param chuncksSize chuncks size
     * @return the created list
     *  @param <T> current object type
     */
    protected List<List<T>> createChunksList(final Stream<T> stream, int chuncksSize) {
        AtomicInteger counter = new AtomicInteger();
        Map<Integer, List<T>> mapOfItems = stream.collect(Collectors
                .groupingBy(t -> counter.getAndIncrement() / chuncksSize));
        return new ArrayList<>(mapOfItems.values());
    }

    /**
     * Builds list of json objects to send via queue
     * @param transferList list containing the created json objects
     * @param items result set from the database
     * @param chuncksSize size of items to add per list
     * @param updateListSize consumer to update the list's size
     * @return the created list.
     * @param <T> current object type
     */
    protected List<String> buildTransferItemList(ArrayList<String> transferList, Page<T> items,
            int chuncksSize, Consumer <Integer> updateListSize ) {
        List<List<T>> listOfItems = createChunksList(items.stream(), chuncksSize);
        listOfItems.stream().forEach(list -> {
            String jsonMessage = createJsonMessageList(list);
            if (nonNull(jsonMessage)) {
                checkAddCreatedMessage(transferList, chuncksSize, list, jsonMessage, updateListSize);
            }
        });
        return transferList;
    }

    /**
     * checks the size of the created message and reduces the item's number if the size in higher than the allowed size.
     * @param transferList current list containing the created json
     * @param chuncksSize number of items per json message
     * @param dataList list of result from the database
     * @param updateListSize consumer to update the list's size
     * @param jsonMessage created json message to check
     * @param <T> type of the current data type
     */
        private void checkAddCreatedMessage(ArrayList<String> transferList, int chuncksSize,
            List<T> dataList, String jsonMessage, Consumer<Integer> updateListSize) {
        if (isSizeValid(jsonMessage.getBytes().length) || chuncksSize == 1) {
            transferList.add(jsonMessage);
        } else {
            log.debug("splitting in two messages: the current message size = {} bigger than the allowed size = {}",
                    jsonMessage.getBytes().length, maxMsgSize);
            Page<T> pageItems = new PageImpl<>(dataList);
            int newChuncksSize = chuncksSize - 1;
            updateListSize.accept(newChuncksSize);
            buildTransferItemList(transferList, pageItems, newChuncksSize, updateListSize);

        }
    }

    /**
     * Check if the size of the created json message is lower than the max size
     * @param currentSize size of the created message
     * @return true is the size is lower than the max size otherwise false
     */
    private boolean isSizeValid(int currentSize) {
        return currentSize <= Integer.parseInt(maxMsgSize);
    }


    /**
    * Creates a jsom message from the given object list
    * @param list the given list containing the objects
    * @return the created json message
    */
    protected String createJsonMessageList(List<T> list) {
        try {
            D messageDto = createTransferDTO(list);
            return createJsonMessage(messageDto);
        } catch (Exception e) {
            log.error("description = {} cause = {}", ExceptionUtils.getMessage(e),
                    ExceptionUtils.getRootCauseMessage(e));
        }
        return null;
    }

    /**
     * Calculates the number of items to send in each json message
     * @param list containing items
     * @param currentListSize current list size
     * @return the calculated list size
     * @param <T> type of data in the list
     */
    protected int calculateJsonListSize(List<T> list, int currentListSize) {
        if (!list.isEmpty() && currentListSize == 0) {
            String jsonMessageList = createJsonMessageList(list);
            if (nonNull(jsonMessageList)) {
                int msgAverageSize = jsonMessageList.getBytes().length / list.size();
                currentListSize = Integer.parseInt(maxMsgSize) / msgAverageSize;
                log.info("Json message contains = {} objects from type = {}", currentListSize, list.get(0).getClass());
            }
            return currentListSize;
        }
        return currentListSize;
    }
}

