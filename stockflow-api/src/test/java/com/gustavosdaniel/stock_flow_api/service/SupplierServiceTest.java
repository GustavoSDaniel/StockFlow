package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepClient;
import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.AddressResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.SupplierContactResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.SupplierResponse;
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
    @DisplayName("Should with sucesso create supplier")
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
                .assertNext(resultado -> {
                    assertNotNull(resultado);
                    assertEquals(supplierId, resultado.id(), "O ID do fornecedor deve ser o esperado");
                    assertEquals(name, resultado.name(), "O nome do fornecedor deve bater");
                    assertEquals(1, resultado.contacts().size(), "Deve conter 1 contato");
                    assertEquals(1, resultado.addresses().size(), "Deve conter 1 endereço");
                })
                .verifyComplete();

        verify(suppliersRepository).existsByCnpj(cnpj);
        verify(suppliersRepository).save(any(Supplier.class));
        verify(supplierContactRepository).saveAll(anyIterable());
        verify(viaCepClient).findByAddressByZipCode(zipCode);
        verify(addressRepository).saveAll(anyIterable());
    }
}