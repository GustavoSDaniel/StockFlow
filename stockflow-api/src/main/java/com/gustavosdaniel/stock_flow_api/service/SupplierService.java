package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepClient;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.*;
import com.gustavosdaniel.stock_flow_api.domain.mapping.SupplierMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.SupplierNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.AddressRepository;
import com.gustavosdaniel.stock_flow_api.repository.SupplierContactRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
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
import java.util.Map;
import java.util.UUID;

@Service
public class SupplierService {

    private final Logger log = LoggerFactory.getLogger(SupplierService.class);
    private final AddressRepository addressRepository;
    private final SupplierContactRepository supplierContactRepository;
    private final SuppliersRepository suppliersRepository;
    private final SupplierMapper supplierMapper;
    private final ViaCepClient viaCepClient;



    public SupplierService(AddressRepository addressRepository, SupplierContactRepository supplierContactRepository, SuppliersRepository suppliersRepository, SupplierMapper supplierMapper, ViaCepClient viaCepClient) {
        this.addressRepository = addressRepository;
        this.supplierContactRepository = supplierContactRepository;
        this.suppliersRepository = suppliersRepository;
        this.supplierMapper = supplierMapper;
        this.viaCepClient = viaCepClient;
    }

    @Transactional
    public Mono<SupplierResponse> createSupplier(SupplierRequest supplierRequest){

        return suppliersRepository.existsByCnpj(supplierRequest.cnpj())
                .doFirst(() -> log.info("Criando um novo fornecedor"))
                .flatMap( existeCnpj -> {

                    if (existeCnpj) return Mono.error(
                            new BusinessRuleException("CNPJ já está cadastrado no sistema."));

                    Supplier newSupplier = supplierMapper.toSupplier(supplierRequest);

                    return suppliersRepository.save(newSupplier);
                    
                })
                .flatMap(supplierSave -> {

                    UUID supplierId = supplierSave.getId();

                    List<SupplierContact> contactsToSave = supplierRequest.contacts().stream()
                            .map(contactRequest -> supplierMapper
                                    .toSupplierContact(supplierId, contactRequest))
                            .toList();

                    Mono<List<SupplierContact>> savedContacts = supplierContactRepository
                            .saveAll(contactsToSave)
                            .collectList();

                    Mono<List<Address>> savedAddresses = Flux.fromIterable(supplierRequest.addresses())
                            .flatMap(addressRequest ->
                                    viaCepClient.findByAddressByZipCode(addressRequest.zipCode())
                                            .map(viaCepResponse ->
                                                    supplierMapper.toAddress(
                                                            supplierId, addressRequest, viaCepResponse)),
                                    3  // limita concorrência ao ViaCEP
                            )
                            .collectList()
                            .flatMap(addressesToSave ->
                                    addressRepository.saveAll(addressesToSave).collectList());

                    return Mono.zip(
                            Mono.just(supplierSave),
                            savedContacts,
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
    public Mono<Page<SupplierSummaryResponse>> getAllSupplier(Pageable pageable){

        return PageUtils.toPage(

                        suppliersRepository.findAllBy(pageable),
                        suppliersRepository.count(),
                        supplierMapper::toSupplierSummaryResponse,
                        pageable
        )
                .doFirst(() -> log.info("Buscando todos os fornecedores"))
                .doOnSuccess(response ->
                        log.info("Total de fornecedores encontrados {}", response.getTotalElements()));
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

                    Mono<List<Address>> address = addressRepository
                            .findAllBySupplierId(supplerId)
                            .collectList();

                    return Mono.zip(
                            Mono.just(supplier),
                            contacts,
                            address
                    ).map(tuple -> supplierMapper
                            .toSupplierResponse(tuple.getT1(), tuple.getT2(), tuple.getT3()));
                })
                .doOnNext(suppler ->
                        log.info("Fornecedor encontrado com sucesso: '{}'", suppler.name()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<SupplierSummaryResponse>> searchSupplierByName(String name, Pageable pageable){

        return PageUtils.toPage(

                        suppliersRepository.searchByName(name, pageable),
                        suppliersRepository.countByName(name),
                        supplierMapper::toSupplierSummaryResponse,
                        pageable
        )
                .doFirst(() -> log.info("Buscando fornecedor pelo nome: {}", name))
                .doOnSuccess(page ->
                        log.info("Busca concluída, {} fornecedores encontrados para o nome {}, " +
                                        "na pagina {}",
                                page.getNumberOfElements(), name, page.getNumber())
                );
    }

    @Transactional(readOnly = true)
    public Mono<Page<SupplierSummaryResponse>> searchSupplierByTradeName(String tradeName, Pageable pageable){

        return PageUtils.toPage(

                        suppliersRepository.searchByTradeName(tradeName, pageable),
                        suppliersRepository.countByTradeName(tradeName),
                        supplierMapper::toSupplierSummaryResponse,
                        pageable

        )
                .doFirst(() -> log.info("Buscando fornecedor pelo nome fantasia: {}", tradeName))
                .doOnSuccess(page -> {
                    log.info(
                            "Busca concluída, {} fornecedores encontrados para o nome fantasia {}, " +
                                    "na pagina {}",
                            page.getNumberOfElements(), tradeName, page.getNumber());
                });
    }

    @Transactional
    public Mono<AddressResponse> addAddress(UUID supplierId,AddressRequest request){

        return suppliersRepository.existsById(supplierId)
                .flatMap(existSupplier -> {

                    if (!existSupplier) return Mono.error(new SupplierNotFoundException());

                    if (request.hasManualAddress()) {

                        Address manualAddress = supplierMapper.toManualAddress(supplierId, request);

                        return addressRepository.save(manualAddress);
                    }

                    return viaCepClient.findByAddressByZipCode(request.zipCode())
                            .map(viaCepResponse ->
                                    supplierMapper.toAddress(supplierId, request, viaCepResponse)
                            )
                            .flatMap(addressRepository::save);
                })
                .doFirst(() -> log.info("Adicionando endereço para o fornecedor: {}", supplierId))
                .map(supplierMapper::toAddressResponse)
                .doOnNext(resultado ->
                        log.info("Endereço: {} adicionado com sucesso para o fornecedor: {}",
                                resultado.street(), supplierId));
    }

    @Transactional
    public Mono<Void> removeAddress(UUID addressId){

        return addressRepository.findById(addressId)
                .switchIfEmpty(Mono.error(
                        new BusinessRuleException("O endereço inserido não existe")))
                .flatMap(addressRepository::delete)
                .doFirst(() -> log.warn("Iniciando processo para deletar endereço: {}", addressId))
                .doOnSuccess(v -> log.info("Endereço deletado com sucesso"));
    }

    @Transactional
    public Mono<SupplierContactResponse> addSupplierContact(
            UUID supplierId, SupplierContactRequest request){

        return suppliersRepository.existsById(supplierId)
                .flatMap(existsSupplier -> {

                    if (!existsSupplier) return Mono.error(new SupplierNotFoundException());

                    SupplierContact  newContact = supplierMapper.toSupplierContact(supplierId, request);

                    return supplierContactRepository.save(newContact);

                })
                .map(supplierMapper::toSupplierContactResponse)
                .doFirst(() -> log.info("Adicionando um novo contato, para o fornecedor: {}", supplierId))
                .doOnNext(resultado ->
                        log.info("Contato: {}, adicionado com sucesso para o fornecedor {}",
                                resultado.id(), supplierId));
    }

    @Transactional
    public Mono<Void> removeContact(UUID contactId){

        return supplierContactRepository.findById(contactId)
                .switchIfEmpty(Mono.error(new BusinessRuleException("O contato inserido não existe")))
                .flatMap(supplierContactRepository::delete)
                .doFirst(() -> log.warn("Iniciando processo para deletar contato: {}", contactId))
                .doOnSuccess(v -> log.info("Contato deletado com sucesso"));
    }

    @Transactional
    public Mono<SupplierUpdateResponse> updateSupplier(UUID supplierId, SupplierUpdateRequest request){

        return suppliersRepository.findById(supplierId)
                .switchIfEmpty(Mono.error(new SupplierNotFoundException()))
                .flatMap(supplier -> {

                    supplierMapper.toSupplierUpdateRequest(supplier, request);
                    return suppliersRepository.save(supplier);
                })
                .map(supplierMapper::toSupplierUpdateResponse)
                .doFirst(() -> log.info("Atualizando informações do fornecedor: {}", supplierId))
                .doOnNext(response ->
                        log.info("Fornecedor: {} atualizado com sucesso", response.name()));
    }

    @Transactional
    public Mono<Void> deleteSupplier(UUID supplierId){

        return suppliersRepository.findById(supplierId)
                .switchIfEmpty(Mono.error(new SupplierNotFoundException()))
                .flatMap(supplier -> {

                    Mono<Void> deleteContacts = supplierContactRepository.deleteAllBySupplierId(supplierId);
                    Mono<Void> deleteAddress = addressRepository.deleteAllBySupplierId(supplierId);

                    return Mono.when(deleteContacts, deleteAddress)
                            .then(suppliersRepository.delete(supplier));

                })
                .doFirst(() -> log.warn("Iniciando processo para deletar fornecedor: {}. " +
                                "Esse processo é irreversível e todos os contatos e endereços serão deletados junto.",
                        supplierId))
                .doOnSuccess(unused -> log.info("Fornecedor e suas dependências deletados com sucesso"));
    }
}
