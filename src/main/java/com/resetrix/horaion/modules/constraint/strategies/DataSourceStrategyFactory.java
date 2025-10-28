package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.exceptions.DataSourceException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSourceStrategyFactory {

    private final List<DataSourceStrategy> strategies;

    public DataSourceStrategyFactory(List<DataSourceStrategy> strategies) {
        this.strategies = strategies;
    }

    public DataSourceStrategy getStrategy(SourceType sourceType) {
        return strategies.stream()
            .filter(strategy -> strategy != null && strategy.supports(sourceType.name()))
            .findFirst()
            .orElseThrow(() -> new DataSourceException("No strategy found for source type: " + sourceType));
    }
}
