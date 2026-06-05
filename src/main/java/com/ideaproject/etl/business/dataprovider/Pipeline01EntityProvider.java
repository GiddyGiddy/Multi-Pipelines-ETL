package com.ideaproject.etl.business.dataprovider;

import com.ideaproject.etl.business.dto.event.Pipeline01EntityTransfer;
import com.ideaproject.etl.business.mapper.Pipeline01EntityMapper;
import com.ideaproject.etl.domain.entities.entity1.Pipeline01Entity;
import com.ideaproject.etl.messaging.MessagePublisher;
import com.ideaproject.etl.persistence.repository.Pipeline01EntityRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ETL provider for Entity1 items.
 */
@Service
@Slf4j
@Getter
public class Pipeline01EntityProvider extends DataProvider<Pipeline01Entity, Pipeline01EntityTransfer> {

    private final Pipeline01EntityRepository pipeline01EntityRepository;
    private final Pipeline01EntityMapper pipeline01EntityMapper;
    private final Extractor<Pipeline01Entity> extractor;
    private final Transformer<List<Pipeline01Entity>, Pipeline01EntityTransfer> transformer;
    private final Loader<String> loader;
    private int jsonListSize;
    private final Consumer<Integer> updateListSize = item -> this.jsonListSize = item;

    public Pipeline01EntityProvider(Pipeline01EntityRepository pipeline01EntityRepository,
            Pipeline01EntityMapper pipeline01EntityMapper,
            MessagePublisher messagePublisher,
            @Value("${messaging.entity1.binding}") String queueBinding) {
        this.pipeline01EntityRepository = pipeline01EntityRepository;
        this.pipeline01EntityMapper = pipeline01EntityMapper;
        this.extractor = new Extractor<>() {
            @Override
            public Page<Pipeline01Entity> extract(Pageable pageable) {
                return pipeline01EntityRepository.findAll(pageable);
            }

            @Override
            public long count() {
                return pipeline01EntityRepository.count();
            }
        };
        this.transformer = list -> {
            Pipeline01EntityTransfer transfer = new Pipeline01EntityTransfer();
            list.forEach(item -> transfer.getPayload().add(pipeline01EntityMapper.mapToPipeline01EntityItemDto(item)));
            return transfer;
        };
        this.loader = payload -> messagePublisher.publish(queueBinding, payload);
    }

    @Override
    protected Page<Pipeline01Entity> getAllItems(Pageable pageable) {
        return extractor.extract(pageable);
    }

    @Override
    public long getItemsCount() {
        return extractor.count();
    }

    @Override
    public boolean startReading(Consumer<String> consumeItem, int pageSize, DataItemPostProcessor<String> postProcessor) {
        Consumer<String> loadAndConsume = payload -> {
            loader.load(payload);
            consumeItem.accept(payload);
        };
        return super.startReading(loadAndConsume, pageSize, postProcessor);
    }

    @Override
    protected List<String> buildTransferItems(Page<Pipeline01Entity> page) {
        ArrayList<String> transfers = new ArrayList<>();
        jsonListSize = calculateJsonListSize(page.stream().toList(), jsonListSize);
        buildTransferItemList(transfers, page, jsonListSize, updateListSize);
        return transfers;
    }

    @Override
    protected Pipeline01EntityTransfer createTransferDTO(List<Pipeline01Entity> list) {
        return transformer.transform(list);
    }
}
