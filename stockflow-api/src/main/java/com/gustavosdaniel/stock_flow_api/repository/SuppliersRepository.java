package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Supplier} entities.
 * <p>
 * Provides queries for searching by name and trade name, checking CNPJ
 * uniqueness, and computing dashboard supplier statistics.
 * </p>
 */
public interface SuppliersRepository extends R2dbcRepository<Supplier, UUID> {

    /**
     * Finds all suppliers with pagination.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link Supplier} entities
     */
    Flux<Supplier> findAllBy(Pageable pageable);

    /**
     * Checks whether a supplier with the given CNPJ already exists.
     *
     * @param cnpj the CNPJ to check
     * @return a {@link Mono} emitting {@code true} if a matching supplier exists
     */
    Mono<Boolean> existsByCnpj(String cnpj);

    /**
     * Finds a single supplier by its CNPJ.
     *
     * @param cnpj the supplier CNPJ
     * @return a {@link Mono} emitting the {@link Supplier}, or empty if not found
     */
    Mono<Supplier> findByCnpj(String cnpj);

    /**
     * Searches suppliers by name using a case-insensitive partial match.
     *
     * @param name     the search term
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Supplier} entities
     */
    @Query("SELECT * FROM suppliers WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Supplier> searchByName(String name, Pageable pageable);

    /**
     * Counts suppliers matching the given name (case-insensitive partial match).
     *
     * @param name the search term
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM suppliers WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    /**
     * Searches suppliers by trade name using a case-insensitive partial match.
     *
     * @param tradeName the search term for trade name
     * @param pageable  pagination parameters
     * @return a {@link Flux} of matching {@link Supplier} entities
     */
    @Query("SELECT * FROM suppliers WHERE trade_name ILIKE CONCAT('%', :tradeName, '%')")
    Flux<Supplier> searchByTradeName(String tradeName, Pageable pageable);

    /**
     * Counts suppliers matching the given trade name (case-insensitive partial match).
     *
     * @param tradeName the search term for trade name
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM suppliers WHERE trade_name ILIKE CONCAT('%', :tradeName, '%')")
    Mono<Long> countByTradeName(String tradeName);

    /**
     * Retrieves dashboard statistics for each supplier: total products supplied
     * and total stock value, ordered by stock value descending.
     *
     * @return a {@link Flux} of {@link DashboardSupplierResponse.SupplierDashboardItem}
     */
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
