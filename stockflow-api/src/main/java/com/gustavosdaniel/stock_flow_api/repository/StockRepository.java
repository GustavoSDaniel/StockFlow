package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface StockRepository extends R2dbcRepository<Stock, UUID> {
}
