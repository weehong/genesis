package com.resetrix.horaion.modules.constraint.strategies;

import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;

import java.util.List;

public interface DataSourceStrategy {

    List<FieldOption> fetchOptions(FieldDefinition fieldDefinition);

    boolean supports(String sourceType);
}
