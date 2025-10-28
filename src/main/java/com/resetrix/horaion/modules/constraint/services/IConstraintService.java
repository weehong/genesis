package com.resetrix.horaion.modules.constraint.services;

import com.resetrix.horaion.shared.services.contracts.IGenericService;

public sealed interface IConstraintService<T, K>
    extends IGenericService<T, K>
    permits ConstraintService {

}
