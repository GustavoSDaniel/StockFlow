package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface SuppliersRepository extends R2dbcRepository<Supplier, UUID> {
}
