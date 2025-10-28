package com.resetrix.horaion.modules.company.services;

import com.resetrix.horaion.shared.services.contracts.IGenericService;

public sealed interface ICompanyService<T, K>
    extends IGenericService<T, K>
    permits CompanyService {

}
