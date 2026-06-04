package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.SupplierResponse;
import com.gustavosdaniel.stock_flow_api.exception.NameExistException;
import com.gustavosdaniel.stock_flow_api.repository.AddressRepository;
import com.gustavosdaniel.stock_flow_api.repository.SupplierContactRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SupplierService {

    private final Logger log = LoggerFactory.getLogger(SupplierService.class);
    private final AddressRepository addressRepository;
    private final SupplierContactRepository supplierContactRepository;
    private final SuppliersRepository suppliersRepository;


    public SupplierService(AddressRepository addressRepository, SupplierContactRepository supplierContactRepository, SuppliersRepository suppliersRepository) {
        this.addressRepository = addressRepository;
        this.supplierContactRepository = supplierContactRepository;
        this.suppliersRepository = suppliersRepository;
    }

    public Mono<SupplierResponse> createSupplier(
            SupplierRequest supplierRequest,
            SupplierContactRequest supplierContactRequest,
            AddressRequest addressRequest
    ){
        return suppliersRepository.existsByCnpj(supplierRequest.cnpj())
                .flatMap( existeCnpj -> {

                    if (existeCnpj) Mono.error(new CnpjExisteException);
                    
                }

    })
}
