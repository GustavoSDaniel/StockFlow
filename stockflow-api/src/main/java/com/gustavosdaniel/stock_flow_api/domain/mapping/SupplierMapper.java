package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.client.viacep.ViaCepResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.*;
import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SupplierMapper {

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

    public Address toAddress(UUID supplierId, AddressRequest request, ViaCepResponse viaCepResponse){

        if (request == null) return null;

        return new Address(
                        supplierId,
                        request.label(),
                        viaCepResponse.logradouro(),
                        request.streetNumber(),
                        request.complement(),
                        viaCepResponse.bairro(),
                        viaCepResponse.localidade(),
                        request.zipCode(),
                        StateUF.fromName(viaCepResponse.uf()),
                        "Brasil",
                        request.isMain()
                );
    }

    public Address toManualAddress(UUID supplierId, AddressRequest request) {

        if (request == null) return null;

        return new Address(

                supplierId,
                request.label(),
                request.street(),
                request.streetNumber(),
                request.complement(),
                request.neighborhood(),
                request.city(),
                request.zipCode(),
                request.stateUF(),
                request.country(),
                request.isMain()
        );
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
                addressResponses,
                supplier.getCreatedAt()

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
