package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.SupplierOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.*;
import com.gustavosdaniel.stock_flow_api.service.SupplierService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController implements SupplierOpenApi {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping()
    public Mono<ResponseEntity<SupplierResponse>> createSupplier(
            @RequestBody @Valid SupplierRequest request)
    {
        return supplierService.createSupplier(request).map(response ->
                ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping()
    public Mono<ResponseEntity<Page<SupplierSummaryResponse>>> allSupplier(
            @ParameterObject
            @PageableDefault(size = 20, sort = "tradeName",direction = Sort.Direction.ASC)
            Pageable pageable)
    {
        return supplierService.getAllSupplier(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/cnpj")
    public Mono<ResponseEntity<SupplierResponse>> findCnpj(@RequestParam String cnpj)
    {
        return supplierService.findSupplierByCnpj(cnpj).map(ResponseEntity::ok);

    }

    @GetMapping("/name")
    public Mono<ResponseEntity<Page<SupplierSummaryResponse>>> searchName(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name",direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return supplierService.searchSupplierByName(name, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/tradeName")
    public Mono<ResponseEntity<Page<SupplierSummaryResponse>>> searchTradeName(
            @RequestParam String tradeName,
            @ParameterObject
            @PageableDefault(size = 20, sort = "tradeName",direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return supplierService.searchSupplierByTradeName(tradeName, pageable).map(ResponseEntity::ok);
    }

    @PostMapping("/{supplierId}/address")
    public Mono<ResponseEntity<AddressResponse>> addAddress(
            @PathVariable UUID supplierId,
            @Valid @RequestBody AddressRequest request)
    {
        return supplierService.addAddress(supplierId, request).map(response ->
                ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @DeleteMapping("/address/{addressId}")
    public Mono<ResponseEntity<Void>> deleteAddress(@PathVariable UUID addressId)
    {
        return supplierService.removeAddress(addressId).thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/{supplierId}/contact")
    public Mono<ResponseEntity<SupplierContactResponse>> addContact(
            @PathVariable UUID supplierId, @Valid @RequestBody SupplierContactRequest request)
    {
        return supplierService.addSupplierContact(supplierId, request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(response)
                );
    }

    @DeleteMapping("/contact/{contactId}")
    public Mono<ResponseEntity<Void>> removeContact(@PathVariable UUID contactId)
    {
        return supplierService.removeContact(contactId).thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/{supplierId}")
    public Mono<ResponseEntity<SupplierUpdateResponse>> updateSupplier(
            @PathVariable UUID supplierId,
            @Valid @RequestBody SupplierUpdateRequest request)
    {
        return supplierService.updateSupplier(supplierId, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{supplierId}")
    public Mono<ResponseEntity<Void>> deleteSupplier(@PathVariable UUID supplierId)
    {
        return supplierService.deleteSupplier(supplierId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
