package com.ideaproject.etl.business.dataprovider;

import com.ideaproject.etl.business.dto.event.Pipeline02EntityTransfer;
import com.ideaproject.etl.business.mapper.Pipeline02EntityMapper;
import com.ideaproject.etl.domain.entities.Entity2.Pipeline02Entity;
import com.ideaproject.etl.messaging.MessagePublisher;
import com.ideaproject.etl.persistence.repository.Pipeline02EntityRepository;
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
 * ETL provider for Entity2 items.
 */
@Service
@Slf4j
@Getter
public class Pipeline02EntityProvider extends DataProvider<Pipeline02Entity, Pipeline02EntityTransfer> {

    private final Pipeline02EntityRepository pipeline02EntityRepository;
    private final Pipeline02EntityMapper pipeline02EntityMapper;
    private final Extractor<Pipeline02Entity> extractor;
    private final Transformer<List<Pipeline02Entity>, Pipeline02EntityTransfer> transformer;
    private final Loader<String> loader;
    private int jsonListSize;
    private final Consumer<Integer> updateListSize = item -> this.jsonListSize = item;

    public Pipeline02EntityProvider(Pipeline02EntityRepository pipeline02EntityRepository,
            Pipeline02EntityMapper pipeline02EntityMapper,
            MessagePublisher messagePublisher,
            @Value("${messaging.entity2.binding}") String queueBinding) {
        this.pipeline02EntityRepository = pipeline02EntityRepository;
        this.pipeline02EntityMapper = pipeline02EntityMapper;
        this.extractor = new Extractor<>() {
            @Override
            public Page<Pipeline02Entity> extract(Pageable pageable) {
                return pipeline02EntityRepository.findAll(pageable);
            }

            @Override
            public long count() {
                return pipeline02EntityRepository.count();
            }
        };
        this.transformer = list -> {
            Pipeline02EntityTransfer transfer = new Pipeline02EntityTransfer();
            list.forEach(item -> transfer.getPayload().add(pipeline02EntityMapper.mapToPipeline02EntityItemDto(item)));
            return transfer;
        };
        this.loader = payload -> messagePublisher.publish(queueBinding, payload);
    }

    @Override
    protected Page<Pipeline02Entity> getAllItems(Pageable pageable) {
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
    protected List<String> buildTransferItems(Page<Pipeline02Entity> page) {
        ArrayList<String> transfers = new ArrayList<>();
        jsonListSize = calculateJsonListSize(page.stream().toList(), jsonListSize);
        buildTransferItemList(transfers, page, jsonListSize, updateListSize);
        return transfers;
    }

    @Override
    protected Pipeline02EntityTransfer createTransferDTO(List<Pipeline02Entity> list) {
        return transformer.transform(list);
    }
}
