package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepClient;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.*;
import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class SupplierMapper {

    private final ViaCepClient viaCepClient;
    private final Logger log = LoggerFactory.getLogger(SupplierMapper.class);

    public SupplierMapper(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public Supplier toSupplier(SupplierRequest request){

        if (request == null) return null;

        return new Supplier(

                request.name(),
                request.cnpj(),
                request.tradeName(),
                request.website(),
                request.minOrderValue(),
                request.notes()
        );
    }

    public SupplierContact toSupplierContact(UUID supplierId, SupplierContactRequest request){

        if (request == null) return null;

        return new SupplierContact(

                supplierId,
                request.contactName(),
                request.email(),
                request.phoneNumber()
        );
    }

    public Mono<Address> toAddress(UUID supplierId, AddressRequest request){

        if (request == null) return Mono.empty();

        return viaCepClient.findByAddressByZipCode(request.zipCode())
                .map(viaCep -> new Address(
                        supplierId,
                        request.label(),
                        viaCep.logradouro(),
                        request.streetNumber(),
                        request.complement(),
                        viaCep.bairro(),
                        viaCep.localidade(),
                        request.zipCode(),
                        StateUF.fromName(viaCep.uf()),
                        "Brasil",
                        request.isMain()
                ))
                .onErrorResume(e -> {
                    log.warn("ViaCEP falhou ou CEP {} não encontrado. Usando fallback manual.",
                            request.zipCode());

                    if (!request.hasManualAddress()){
                        return Mono.error(new BusinessRuleException(
                                "ViaCEP indisponível. Preencha os campos: street," +
                                        " neighborhood, city e stateUF manualmente."
                        ));
                    }

                    return Mono.just(new Address(
                            supplierId,
                            request.label(),
                            request.street(),
                            request.streetNumber(),
                            request.complement(),
                            request.neighborhood(),
                            request.city(),
                            request.zipCode(),
                            request.stateUF(),
                            request.country() != null ? request.country() : "Brasil",
                            request.isMain()
                    ));
                });
    }

    public SupplierResponse toSupplierResponse(
            Supplier supplier, List<SupplierContact> contacts, List<Address> addresses){

        if (supplier == null) return null;

        List<SupplierContactResponse> contactResponses = contacts.stream()
                .map(this::toSupplierContactResponse)
                .toList();

        List<AddressResponse> addressResponses = addresses.stream()
                .map(this::toAddressResponse)
                .toList();

        return new SupplierResponse(

                supplier.getId(),
                supplier.getName(),
                supplier.getCnpj(),
                supplier.getTradeName(),
                contactResponses,
                supplier.getWebsite(),
                supplier.getMinOrderValue(),
                supplier.getNotes(),
                addressResponses

        );
    }

    public SupplierContactResponse toSupplierContactResponse(SupplierContact supplierContact){

        if (supplierContact == null) return null;

        return new SupplierContactResponse(

                supplierContact.getId(),
                supplierContact.getContactName(),
                supplierContact.getEmail(),
                supplierContact.getPhoneNumber()
        );
    }

    public AddressResponse toAddressResponse(Address address){

        if (address == null) return null;

        return new AddressResponse(

                address.getId(),
                address.getLabel(),
                address.getStreet(),
                address.getStreetNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getZipCode(),
                address.getStateUF(),
                address.getCountry(),
                address.isMain()
        );
    }

    public SupplierSummaryResponse toSupplierSummaryResponse(Supplier supplier){

        if (supplier == null) return null;

        return new SupplierSummaryResponse(

                supplier.getId(),
                supplier.getCnpj(),
                supplier.getName(),
                supplier.getTradeName()
        );

    }

    public void toSupplierUpdateRequest(Supplier supplier, SupplierUpdateRequest request){

        if (request.tradeName() != null && !request.tradeName().isBlank())
            supplier.setTradeName(request.tradeName());

        if (request.website() != null && !request.website().isBlank())
            supplier.setWebsite(request.website());

        if (request.minOrderValue() != null) supplier.setMinOrderValue(request.minOrderValue());

        if (request.notes() != null && !request.notes().isBlank()) supplier.setNotes(request.notes());

    }

    public SupplierUpdateResponse toSupplierUpdateResponse(Supplier supplier){

        if (supplier == null) return null;

        return new SupplierUpdateResponse(

                supplier.getId(),
                supplier.getName(),
                supplier.getCnpj(),
                supplier.getTradeName(),
                supplier.getWebsite(),
                supplier.getMinOrderValue(),
                supplier.getNotes()
        );
    }
}
