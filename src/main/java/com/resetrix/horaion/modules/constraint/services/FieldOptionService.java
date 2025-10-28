package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.strategies.DataSourceStrategy;
import com.resetrix.horaion.modules.constraint.strategies.DataSourceStrategyFactory;
import com.resetrix.horaion.shared.helpers.ServiceOperationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FieldOptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldOptionService.class);

    private final DataSourceStrategyFactory strategyFactory;

    public FieldOptionService(DataSourceStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public List<FieldOption> resolveOptions(FieldDefinition fieldDefinition) {
        return ServiceOperationExecutor.execute(() -> {
            LOGGER.debug("Resolving options for field '{}' with source type '{}'",
                         fieldDefinition.name(), fieldDefinition.sourceType());

            DataSourceStrategy strategy = strategyFactory.getStrategy(fieldDefinition.sourceType());
            List<FieldOption> options = strategy.fetchOptions(fieldDefinition);

            LOGGER.debug("Resolved {} options for field '{}'", options.size(), fieldDefinition.name());
            return options;

        }, "resolving field options", DataSourceException.class);
    }
}
