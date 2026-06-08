package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.SupplierResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.SupplierSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.mapping.SupplierMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.SupplierNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.AddressRepository;
import com.gustavosdaniel.stock_flow_api.repository.SupplierContactRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierService {

    private final Logger log = LoggerFactory.getLogger(SupplierService.class);
    private final AddressRepository addressRepository;
    private final SupplierContactRepository supplierContactRepository;
    private final SuppliersRepository suppliersRepository;
    private final SupplierMapper supplierMapper;


    public SupplierService(AddressRepository addressRepository, SupplierContactRepository supplierContactRepository, SuppliersRepository suppliersRepository, SupplierMapper supplierMapper) {
        this.addressRepository = addressRepository;
        this.supplierContactRepository = supplierContactRepository;
        this.suppliersRepository = suppliersRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional
    public Mono<SupplierResponse> createSupplier(SupplierRequest supplierRequest){

        return suppliersRepository.existsByCnpj(supplierRequest.cnpj())
                .doFirst(() -> log.info("Criando um novo fornecedor"))
                .flatMap( existeCnpj -> {

                    if (existeCnpj) return Mono.error(
                            new BusinessRuleException("CNPJ já está cadastrado no sistema."));

                    Supplier supplierSave = supplierMapper.toSupplier(supplierRequest);

                    return suppliersRepository.save(supplierSave);
                    
                })
                .flatMap(supplierSave -> {

                    UUID supplierId = supplierSave.getId();

                    List<SupplierContact> contactsToSave = supplierRequest.contacts().stream()
                            .map(contactRequest -> supplierMapper
                                    .toSupplierContact(supplierId, contactRequest))
                            .toList();

                    Mono<List<SupplierContact>> salvedContacts = supplierContactRepository
                            .saveAll(contactsToSave)
                            .collectList();

                    Mono<List<Address>> savedAddresses = Flux.fromIterable(supplierRequest.addresses())
                            .flatMap(addressRequest -> supplierMapper
                                    .toAddress(supplierId, addressRequest))
                            .collectList()
                            .flatMap(addressesToSave -> addressRepository
                                    .saveAll(addressesToSave).collectList());

                    return Mono.zip(
                            Mono.just(supplierSave),
                            salvedContacts,
                            savedAddresses
                    ).map(tuple -> {

                        return supplierMapper
                                .toSupplierResponse(tuple.getT1(), tuple.getT2(), tuple.getT3());

                    });

                })
                .doOnNext(response ->
                        log.info("Fornecedor criado com sucesso: {}", response.name())
                );
    }

    @Transactional(readOnly = true)
    public Mono<SupplierResponse> findSupplierByCnpj(String cnpj){

        return suppliersRepository.findByCnpj(cnpj)
                .switchIfEmpty(Mono.error(new SupplierNotFoundException()))
                .doFirst(() -> log.info("Buscando fornecedor pelo numero do CNPJ {}", cnpj))
                .flatMap(supplier -> {

                    UUID supplerId = supplier.getId();

                    Mono<List<SupplierContact>> contacts = supplierContactRepository
                            .findAllBySupplierId(supplerId)
                            .collectList();

                    Mono<List<Address>> address = addressRepository.findAllBySupplierId(supplerId)
                            .collectList();

                    return Mono.zip(
                            Mono.just(supplier),
                            contacts,
                            address
                    ).map(tuple -> supplierMapper
                            .toSupplierResponse(tuple.getT1(), tuple.getT2(), tuple.getT3()));
                })
                .doOnNext(suppler ->
                        log.info("Fornecedor encontrado com sucesso {}", suppler.name()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<SupplierSummaryResponse>> searchSupplierByName(String name, Pageable pageable){

        return suppliersRepository.searchByName(name, pageable)
                .doFirst(() -> log.info("Buscando fornecedor pelo nome: {}", name))
                .map(supplierMapper::toSupplierSummaryResponse)
                .collectList()
                .zipWith(suppliersRepository.countByName(name))
                .map(tuple -> (Page<SupplierSummaryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doOnSuccess(page ->
                        log.info("Busca concluída, {} fornecedores encontrados para o nome {}, na pagina {}",
                                page.getNumberOfElements(), name, pageable.getPageNumber())
                );
    }

    @Transactional(readOnly = true)
    public Mono<Page<SupplierSummaryResponse>> searchSupplierByTradeName(String tradeName, Pageable pageable){

        return suppliersRepository.searchByTradeName(tradeName, pageable)
                .doFirst(() -> log.info("Buscando fornecedor pelo nome fantasia: {}", tradeName))
                .map(supplierMapper::toSupplierSummaryResponse)
                .collectList()
                .zipWith(suppliersRepository.countByTradeName(tradeName))
                .map(tuple -> (Page<SupplierSummaryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doOnSuccess(page -> {
                    log.info(
                            "Busca concluída, {} fornecedores encontrados para o nome fantasia {}, " +
                                    "na pagina {}",
                            page.getNumberOfElements(), tradeName, pageable.getPageNumber());
                });
    }
}
