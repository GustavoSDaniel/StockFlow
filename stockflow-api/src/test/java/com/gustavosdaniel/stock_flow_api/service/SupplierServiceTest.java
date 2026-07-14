package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepClient;
import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.*;
import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import com.gustavosdaniel.stock_flow_api.domain.mapping.SupplierMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import com.gustavosdaniel.stock_flow_api.repository.AddressRepository;
import com.gustavosdaniel.stock_flow_api.repository.SupplierContactRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private  AddressRepository addressRepository;

    @Mock
    private  SupplierContactRepository supplierContactRepository;

    @Mock
    private  SuppliersRepository suppliersRepository;

    @Mock
    private  SupplierMapper supplierMapper;

    @Mock
    private  ViaCepClient viaCepClient;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    @DisplayName("Should create supplier successfully")
    void createSupplier(){

        UUID contactId = UUID.randomUUID();
        String contactName = "Gustavo";
        String email = "gustavo@gmail.com";
        String phoneNumber = "16122334455";

        UUID addressId = UUID.randomUUID();
        String label = "Barraçao 1";
        String street = "Rua dos programadores";
        String streetNumber = "2233333";
        String complement = "Perto do mar";
        String neighborhood = "Bairro dos programadores";
        String city = "Franca";
        String zipCode = "11222666";
        String country = "Brasil";
        StateUF uf = StateUF.SP;
        Boolean isMain = false;

        UUID supplierId = UUID.randomUUID();
        String name = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        SupplierContactRequest contactRequest = new SupplierContactRequest(
               contactName, email, phoneNumber
        );

        List<SupplierContactRequest> contactRequests = List.of(contactRequest);

        AddressRequest addressRequest = new AddressRequest(label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain);

        List<AddressRequest> addresses = List.of(addressRequest);

        Address newAddress = new Address(
                supplierId, label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );
        ReflectionTestUtils.setField(newAddress, "id", addressId);

        List<Address> addressList = List.of(newAddress);

        AddressResponse addressResponse = new AddressResponse(
                addressId, label, street, streetNumber, complement, neighborhood, city, zipCode,
                uf, country, isMain
        );

        List<AddressResponse> addressResponseList = List.of(addressResponse);

        SupplierRequest request = new SupplierRequest(
                name, cnpj, tradeName, contactRequests,
                null, minOrderValue, null, addresses
        );

        Supplier newSupplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(newSupplier, "id", supplierId);

        SupplierContact contact = new SupplierContact(
                supplierId, contactName, email, phoneNumber
        );
        ReflectionTestUtils.setField(contact, "id", contactId);

        List<SupplierContact> contacts = List.of(contact);

        SupplierContactResponse contactResponse = new SupplierContactResponse(
                contactId, contactName, email, phoneNumber
        );

        List<SupplierContactResponse> supplierContactResponses = List.of(contactResponse);

        ViaCepResponse viaCepResponse = new ViaCepResponse(zipCode, street, complement, neighborhood,
                city, "SP", false);

        SupplierResponse response = new SupplierResponse(
                supplierId, name, cnpj, tradeName, supplierContactResponses,
                null, minOrderValue, null, addressResponseList
        );

        when(suppliersRepository.existsByCnpj(cnpj)).thenReturn(Mono.just(false));
        when(supplierMapper.toSupplier(request)).thenReturn(newSupplier);
        when(suppliersRepository.save(any(Supplier.class))).thenReturn(Mono.just(newSupplier));
        when(supplierMapper.toSupplierContact(supplierId, contactRequest)).thenReturn(contact);
        when(supplierContactRepository.saveAll(anyIterable()))
                .thenReturn(Flux.fromIterable(contacts));
        when(viaCepClient.findByAddressByZipCode(addressRequest.zipCode()))
                .thenReturn(Mono.just(viaCepResponse));
        when(supplierMapper.toAddress(supplierId, addressRequest, viaCepResponse)).thenReturn(newAddress);
        when(addressRepository.saveAll(anyIterable())).thenReturn(Flux.fromIterable(addressList));
        when(supplierMapper.toSupplierResponse(newSupplier, contacts, addressList)).thenReturn(response);

        Mono<SupplierResponse> output = supplierService.createSupplier(request);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(supplierId, result.id(), "O ID do fornecedor deve ser o esperado");
                    assertEquals(name, result.name(), "O nome do fornecedor deve bater");
                    assertEquals(1, result.contacts().size(), "Deve conter 1 contato");
                    assertEquals(1, result.addresses().size(), "Deve conter 1 endereço");
                })
                .verifyComplete();

        verify(suppliersRepository).existsByCnpj(cnpj);
        verify(suppliersRepository).save(any(Supplier.class));
        verify(supplierContactRepository).saveAll(anyIterable());
        verify(viaCepClient).findByAddressByZipCode(zipCode);
        verify(addressRepository).saveAll(anyIterable());
    }

    @Test
    @DisplayName("Should get all suppliers successfully")
    void allSupplier(){

        UUID supplierId = UUID.randomUUID();
        String cnpj = "11122233344455";
        String name = "Fornecedor";
        String tradeName = "Nome fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(532);

        UUID supplierId2 = UUID.randomUUID();
        String cnpj2 = "11122233344455";
        String name2 = "Fornecedor";
        String tradeName2 = "Nome fantasia";

        Pageable pageable = Pageable.unpaged();

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        Supplier supplier2 = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier2, "id", supplierId2);


        SupplierSummaryResponse supplierSummaryResponse = new SupplierSummaryResponse(
                supplierId, cnpj, name, tradeName
        );

        SupplierSummaryResponse supplierSummaryResponse2 = new SupplierSummaryResponse(
                supplierId2, cnpj2, name2, tradeName2
        );

        when(suppliersRepository.findAllBy(pageable)).thenReturn(Flux.just(supplier, supplier2));
        when(suppliersRepository.count()).thenReturn(Mono.just(2L));
        when(supplierMapper.toSupplierSummaryResponse(supplier)).thenReturn(supplierSummaryResponse);
        when(supplierMapper.toSupplierSummaryResponse(supplier2)).thenReturn(supplierSummaryResponse2);

        Mono<Page<SupplierSummaryResponse>> output = supplierService.getAllSupplier(pageable);

        StepVerifier.create(output)
                .assertNext(result ->{

                    assertNotNull(result);
                    assertEquals(2L, result.getTotalElements(),
                            "O banco deve reportar 2 elementos no total");
                    assertEquals(2, result.getContent().size(),
                            "A página deve conter exatamente 2 DTOs");
                    assertEquals(supplierId, result.getContent().get(0).id(),
                            "O ID do primeiro deve bater");
                    assertEquals(supplierId2, result.getContent().get(1).id(),
                            "O ID do segundo deve bater");
                })
                .verifyComplete();

        verify(suppliersRepository).findAllBy(pageable);
        verify(suppliersRepository).count();
        verify(supplierMapper).toSupplierSummaryResponse(supplier);
        verify(supplierMapper).toSupplierSummaryResponse(supplier2);
    }

    @Test
    @DisplayName("Should find supplier by CNPJ successfully")
    void findSupplierCnpj(){

        UUID contactId = UUID.randomUUID();
        String contactName = "Gustavo";
        String email = "gustavo@gmail.com";
        String phoneNumber = "16122334455";

        UUID addressId = UUID.randomUUID();
        String label = "Barraçao 1";
        String street = "Rua dos programadores";
        String streetNumber = "2233333";
        String complement = "Perto do mar";
        String neighborhood = "Bairro dos programadores";
        String city = "Franca";
        String zipCode = "11222666";
        String country = "Brasil";
        StateUF uf = StateUF.SP;
        Boolean isMain = false;

        UUID supplierId = UUID.randomUUID();
        String cnpj = "11122233344455";
        String name = "Fornecedor";
        String tradeName = "Nome fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(532);

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        SupplierContact contact = new SupplierContact(
                supplierId, contactName, email, phoneNumber
        );
        ReflectionTestUtils.setField(contact, "id", contactId);

        List<SupplierContact> contacts = List.of(contact);

        Address address = new Address(
                supplierId, label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );
        ReflectionTestUtils.setField(address, "id", addressId);

        List<Address> addressList = List.of(address);

        AddressResponse addressResponse = new AddressResponse(
                addressId, label, street, streetNumber, complement, neighborhood, city, zipCode,
                uf, country, isMain
        );

        List<AddressResponse> addressResponseList = List.of(addressResponse);

        SupplierContactResponse contactResponse = new SupplierContactResponse(
                contactId, contactName, email, phoneNumber
        );

        List<SupplierContactResponse> supplierContactResponses = List.of(contactResponse);

        SupplierResponse response = new SupplierResponse(
                supplierId, name, cnpj, tradeName, supplierContactResponses,
                null, minOrderValue, null, addressResponseList
        );

        when(suppliersRepository.findByCnpj(cnpj)).thenReturn(Mono.just(supplier));
        when(supplierContactRepository.findAllBySupplierId(supplierId)).thenReturn(Flux.just(contact));
        when(addressRepository.findAllBySupplierId(supplierId)).thenReturn(Flux.just(address));
        when(supplierMapper.toSupplierResponse(supplier, contacts, addressList)).thenReturn(response);

        Mono<SupplierResponse> output = supplierService.findSupplierByCnpj(cnpj);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(output);
                    assertEquals(supplier.getId(), result.id(), "O ID tem que ser o mesmo");
                })
                .verifyComplete();

        verify(suppliersRepository).findByCnpj(cnpj);
        verify(supplierContactRepository).findAllBySupplierId(supplierId);
        verify(addressRepository).findAllBySupplierId(supplierId);
        verify(supplierMapper).toSupplierResponse(supplier, contacts, addressList);
    }

    @Test
    @DisplayName("Should find supplier by name successfully")
    void searchSupplierByName(){

        UUID supplierId = UUID.randomUUID();
        String cnpj = "11122233344455";
        String name = "Fornecedor";
        String tradeName = "Nome fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(532);

        Pageable pageable = Pageable.unpaged();

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        SupplierSummaryResponse response = new SupplierSummaryResponse(
                supplierId, cnpj, name, tradeName
        );

        when(suppliersRepository.searchByName(name, pageable)).thenReturn(Flux.just(supplier));
        when(suppliersRepository.countByName(name)).thenReturn(Mono.just(1L));
        when(supplierMapper.toSupplierSummaryResponse(supplier)).thenReturn(response);

        Mono<Page<SupplierSummaryResponse>> output = supplierService.searchSupplierByName(name, pageable);

        StepVerifier.create(output)
                .assertNext(result ->{
                    assertNotNull(result);
                    assertEquals(1, result.getTotalElements(),
                            "O banco deve retornar 1 elemento");
                    assertEquals(1, result.getContent().size(),
                            "Deve retornar 1 DTO");
                    assertEquals(supplierId, result.getContent().get(0).id(),
                            "O ID do fornecedor deve ser o mesmo que o 'supplierId'");
                })
                .verifyComplete();

        verify(suppliersRepository).searchByName(name, pageable);
        verify(suppliersRepository).countByName(name);
        verify(supplierMapper).toSupplierSummaryResponse(supplier);

    }

    @Test
    @DisplayName("Should find supplier by trade name successfully")
    void searchSupplierByTradeName(){

        UUID supplierId = UUID.randomUUID();
        String cnpj = "11122233344455";
        String name = "Fornecedor";
        String tradeName = "Nome fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(532);

        Pageable pageable = Pageable.unpaged();

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        SupplierSummaryResponse response = new SupplierSummaryResponse(
                supplierId, cnpj, name, tradeName
        );

        when(suppliersRepository.searchByTradeName(tradeName, pageable)).thenReturn(Flux.just(supplier));
        when(suppliersRepository.countByTradeName(tradeName)).thenReturn(Mono.just(1L));
        when(supplierMapper.toSupplierSummaryResponse(supplier)).thenReturn(response);

        Mono<Page<SupplierSummaryResponse>> output = supplierService
                .searchSupplierByTradeName(tradeName, pageable);

        StepVerifier.create(output)
                .assertNext(result ->{
                    assertNotNull(result);
                    assertEquals(1, result.getTotalElements(),
                            "O banco deve retornar 1 elemento");
                    assertEquals(1, result.getContent().size(),
                            "Deve retornar 1 DTO");
                    assertEquals(supplierId, result.getContent().get(0).id(),
                            "O ID do fornecedor deve ser o mesmo que o 'supplierId'");
                })
                .verifyComplete();

        verify(suppliersRepository).searchByTradeName(tradeName, pageable);
        verify(suppliersRepository).countByTradeName(tradeName);
        verify(supplierMapper).toSupplierSummaryResponse(supplier);

    }

    @Test
    @DisplayName("Should add address successfully")
    void addAddressManual(){

        UUID supplierId = UUID.randomUUID();
        String cnpj = "11122233344455";
        String name = "Fornecedor";
        String tradeName = "Nome fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(532);

        UUID addressId = UUID.randomUUID();
        String label = "Barraçao 1";
        String street = "Rua dos programadores";
        String streetNumber = "2233333";
        String complement = "Perto do mar";
        String neighborhood = "Bairro dos programadores";
        String city = "Franca";
        String zipCode = "11222666";
        String country = "Brasil";
        StateUF uf = StateUF.SP;
        Boolean isMain = false;

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        AddressRequest request = new AddressRequest(
                label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );

        Address address = new Address(
                supplierId, label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );
        ReflectionTestUtils.setField(address, "id", addressId);

        AddressResponse response = new AddressResponse(
                addressId, label, street, streetNumber, complement, neighborhood, city, zipCode,
                uf, country, isMain
        );

        when(suppliersRepository.existsById(supplierId)).thenReturn(Mono.just(true));
        when(supplierMapper.toManualAddress(supplierId, request)).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(Mono.just(address));
        when(supplierMapper.toAddressResponse(address)).thenReturn(response);

        Mono<AddressResponse> output = supplierService.addAddress(supplierId, request);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(address.getId(), result.id(), "O ID deve ser o mesmo");

                })
                .verifyComplete();

        verify(suppliersRepository).existsById(supplierId);
        verify(supplierMapper).toManualAddress(supplierId, request);
        verify(addressRepository).save(any(Address.class));
        verify(supplierMapper).toAddressResponse(address);
    }

    @Test
    @DisplayName("Should add address via CEP successfully")
    void addAddressViaCep(){

        UUID supplierId = UUID.randomUUID();
        String cnpj = "11122233344455";
        String name = "Fornecedor";
        String tradeName = "Nome fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(532);

        UUID addressId = UUID.randomUUID();
        String label = "Barraçao 1";
        String street = "Rua dos programadores";
        String streetNumber = "2233333";
        String complement = "Perto do mar";
        String neighborhood = "Bairro dos programadores";
        String city = "Franca";
        String zipCode = "11222666";
        String country = "Brasil";
        StateUF uf = StateUF.SP;
        Boolean isMain = false;

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        AddressRequest request = new AddressRequest(
                label, null, streetNumber, complement, null,
                null, zipCode, null ,country, isMain
        );

        Address address = new Address(
                supplierId, label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );
        ReflectionTestUtils.setField(address, "id", addressId);

        AddressResponse response = new AddressResponse(
                addressId, label, street, streetNumber, complement, neighborhood, city, zipCode,
                uf, country, isMain
        );

        ViaCepResponse viaCepResponse = new ViaCepResponse(zipCode, street, complement, neighborhood,
                city, "SP", false);

        when(suppliersRepository.existsById(supplierId)).thenReturn(Mono.just(true));
        when(viaCepClient.findByAddressByZipCode(request.zipCode()))
                .thenReturn(Mono.just(viaCepResponse));
        when(supplierMapper.toAddress(supplierId, request, viaCepResponse)).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(Mono.just(address));
        when(supplierMapper.toAddressResponse(address)).thenReturn(response);

        Mono<AddressResponse> output = supplierService.addAddress(supplierId, request);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(address.getId(), result.id(), "O ID deve ser o mesmo");
                    assertEquals(street, result.street(), "A rua deve ser a rua do ViaCEP");

                })
                .verifyComplete();

        verify(suppliersRepository).existsById(supplierId);
        verify(viaCepClient).findByAddressByZipCode(request.zipCode());
        verify(supplierMapper).toAddress(supplierId, request, viaCepResponse);
        verify(addressRepository).save(any(Address.class));
        verify(supplierMapper).toAddressResponse(address);
    }

    @Test
    @DisplayName("Should delete address successfully")
    void deleteAddress(){

        UUID supplierId = UUID.randomUUID();

        UUID addressId = UUID.randomUUID();
        String label = "Barraçao 1";
        String street = "Rua dos programadores";
        String streetNumber = "2233333";
        String complement = "Perto do mar";
        String neighborhood = "Bairro dos programadores";
        String city = "Franca";
        String zipCode = "11222666";
        String country = "Brasil";
        StateUF uf = StateUF.SP;
        Boolean isMain = false;

        Address address = new Address(
                supplierId, label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );
        ReflectionTestUtils.setField(address, "id", addressId);

        when(addressRepository.findById(addressId)).thenReturn(Mono.just(address));
        when(addressRepository.delete(any(Address.class))).thenReturn(Mono.empty());

        Mono<Void> output = supplierService.removeAddress(addressId);

        StepVerifier.create(output)
                .verifyComplete();

        verify(addressRepository).findById(addressId);
        verify(addressRepository).delete(any(Address.class));
    }

    @Test
    @DisplayName("Should add contact successfully")
    void addContact(){

        UUID supplierId = UUID.randomUUID();

        UUID contactId = UUID.randomUUID();
        String contactName = "Gustavo";
        String email = "gustavo@gmail.com";
        String phoneNumber = "16122334455";

        SupplierContactRequest request = new SupplierContactRequest(
                contactName, email, phoneNumber
        );

        SupplierContact newContact = new SupplierContact(
                supplierId, contactName, email, phoneNumber
        );
        ReflectionTestUtils.setField(newContact, "id", contactId);

        SupplierContactResponse response = new SupplierContactResponse(
                contactId, contactName, email, phoneNumber
        );

        when(suppliersRepository.existsById(supplierId)).thenReturn(Mono.just(true));
        when(supplierMapper.toSupplierContact(supplierId, request)).thenReturn(newContact);
        when(supplierContactRepository.save(any(SupplierContact.class))).thenReturn(Mono.just(newContact));
        when(supplierMapper.toSupplierContactResponse(newContact)).thenReturn(response);

        Mono<SupplierContactResponse> output = supplierService.addSupplierContact(supplierId, request);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(newContact.getId(), result.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(suppliersRepository).existsById(supplierId);
        verify(supplierMapper).toSupplierContact(supplierId, request);
        verify(supplierContactRepository).save(any(SupplierContact.class));
        verify(supplierMapper).toSupplierContactResponse(newContact);

    }

    @Test
    @DisplayName("Should remove contact successfully")
    void removeContact(){

        UUID supplierId = UUID.randomUUID();

        UUID contactId = UUID.randomUUID();
        String contactName = "Gustavo";
        String email = "gustavo@gmail.com";
        String phoneNumber = "16122334455";

        SupplierContact contact = new SupplierContact(
                supplierId, contactName, email, phoneNumber
        );
        ReflectionTestUtils.setField(contact, "id", contactId);

        when(supplierContactRepository.findById(contactId)).thenReturn(Mono.just(contact));
        when(supplierContactRepository.delete(any(SupplierContact.class))).thenReturn(Mono.empty());

        Mono<Void> output = supplierService.removeContact(contactId);

        StepVerifier.create(output)
                .verifyComplete();

        verify(supplierContactRepository).findById(contactId);
        verify(supplierContactRepository).delete(any(SupplierContact.class));
    }

    @Test
    @DisplayName("Should update supplier successfully")
    void updateSupplier(){

        UUID supplierId = UUID.randomUUID();
        String name = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        String tradeNameUpdate = "Nome fantazia atualizada";
        String webSite = "meusite.com";
        BigDecimal minOrderValueUpdate = BigDecimal.valueOf(200);

        SupplierUpdateRequest request = new SupplierUpdateRequest(
                tradeNameUpdate,webSite, minOrderValueUpdate, null
        );

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        SupplierUpdateResponse response = new SupplierUpdateResponse(
                supplierId, name, cnpj, tradeNameUpdate, webSite, minOrderValueUpdate, null
        );

        when(suppliersRepository.findById(supplierId)).thenReturn(Mono.just(supplier));
        doNothing().when(supplierMapper)
                .toSupplierUpdateRequest(any(Supplier.class), any(SupplierUpdateRequest.class));
        when(suppliersRepository.save(any(Supplier.class))).thenReturn(Mono.just(supplier));
        when(supplierMapper.toSupplierUpdateResponse(supplier)).thenReturn(response);

        Mono<SupplierUpdateResponse> output = supplierService.updateSupplier(supplierId, request);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(supplier.getId(), result.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(suppliersRepository).findById(supplierId);
        verify(suppliersRepository, times(1)).findById(supplierId);
        verify(suppliersRepository).save(any(Supplier.class));
        verify(suppliersRepository, times(1)).save(any(Supplier.class));
        verify(supplierMapper).toSupplierUpdateResponse(supplier);

    }

    @Test
    @DisplayName("Should delete supplier successfully")
    void deleteSupplier(){

        UUID contactId = UUID.randomUUID();
        String contactName = "Gustavo";
        String email = "gustavo@gmail.com";
        String phoneNumber = "16122334455";

        UUID addressId = UUID.randomUUID();
        String label = "Barraçao 1";
        String street = "Rua dos programadores";
        String streetNumber = "2233333";
        String complement = "Perto do mar";
        String neighborhood = "Bairro dos programadores";
        String city = "Franca";
        String zipCode = "11222666";
        String country = "Brasil";
        StateUF uf = StateUF.SP;
        Boolean isMain = false;

        UUID supplierId = UUID.randomUUID();
        String name = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        Supplier supplier = new Supplier(name, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        SupplierContact contact = new SupplierContact(
                supplierId, contactName, email, phoneNumber
        );
        ReflectionTestUtils.setField(contact, "id", contactId);

        Address address = new Address(
                supplierId, label, street, streetNumber, complement, neighborhood,
                city, zipCode, uf ,country, isMain
        );
        ReflectionTestUtils.setField(address, "id", addressId);

        when(suppliersRepository.findById(supplierId)).thenReturn(Mono.just(supplier));
        when(supplierContactRepository.deleteAllBySupplierId(supplierId)).thenReturn(Mono.empty());
        when(addressRepository.deleteAllBySupplierId(supplierId)).thenReturn(Mono.empty());
        when(suppliersRepository.delete(any(Supplier.class))).thenReturn(Mono.empty());

        Mono<Void> output = supplierService.deleteSupplier(supplierId);

        StepVerifier.create(output)
                .verifyComplete();

        verify(suppliersRepository).findById(supplierId);
        verify(suppliersRepository, times(1)).findById(supplierId);
        verify(supplierContactRepository).deleteAllBySupplierId(supplierId);
        verify(addressRepository).deleteAllBySupplierId(supplierId);
        verify(suppliersRepository).delete(any(Supplier.class));

    }
}