package com.gustavosdaniel.stock_flow_api.util.cache;

/**
 * Constants defining cache key names for the dashboard feature.
 * <p>
 * All keys share the {@link #PREFIX} {@code "dashboard:"} so they can be
 * bulk-evicted via {@link DashboardCacheManager#evictAllDashboards()}.
 * </p>
 */
public class DashboardCacheKeys {

    private DashboardCacheKeys(){}

    /** Cache key for the overview dashboard. */
    public static final String OVERVIEW = "dashboard:overview";
    /** Cache key for the stock dashboard. */
    public static final String STOCK = "dashboard:stock";
    /** Cache key for the movements dashboard. */
    public static final String MOVEMENTS = "dashboard:movements";
    /** Cache key for the supplier dashboard. */
    public static final String SUPPLIER = "dashboard:supplier";

    /** Common prefix for all dashboard cache keys. */
    public static final String PREFIX = "dashboard:";


}
