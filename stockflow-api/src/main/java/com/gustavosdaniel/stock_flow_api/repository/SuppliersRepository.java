package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SuppliersRepository extends R2dbcRepository<Supplier, UUID> {

    Flux<Supplier> findAllBy(Pageable pageable);

    Mono<Boolean> existsByCnpj(String cnpj);

    Mono<Supplier> findByCnpj(String cnpj);

    @Query("SELECT * FROM suppliers WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Supplier> searchByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM suppliers WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    @Query("SELECT * FROM suppliers WHERE trade_name ILIKE CONCAT('%', :tradeName, '%')")
    Flux<Supplier> searchByTradeName(String tradeName, Pageable pageable);

    @Query("SELECT COUNT(*) FROM suppliers WHERE trade_name ILIKE CONCAT('%', :tradeName, '%')")
    Mono<Long> countByTradeName(String tradeName);

    @Query("""
      SELECT
          sup.id as id,
          sup.name as name,
          COUNT(DISTINCT p.id) as total_products,
          COALESCE(SUM(st.current_quantity * p.cost_price), 0) as total_stock_value
      FROM suppliers sup
      LEFT JOIN products p ON p.supplier_id = sup.id
      LEFT JOIN stocks st ON st.product_id = p.id
      GROUP BY sup.id, sup.name
      ORDER BY total_stock_value DESC
      """)
    Flux<DashboardSupplierResponse.SupplierDashboardItem> getDashboardSupplierStats();

}
