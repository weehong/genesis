package com.resetrix.genesis.modules.company.services;

import com.resetrix.genesis.shared.services.contracts.IGenericService;

public sealed interface ICompanyService<T, K>
    extends IGenericService<T, K>
    permits CompanyService {

}
