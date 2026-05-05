-- =========================================================================
-- V1__create_extensions_and_enums.sql
-- Extensões e tipos enumerados do sistema
-- =========================================================================

-- Extensão para geração nativa de UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- TIPOS ENUMERADOS (ENUMS)
-- ==========================================

CREATE TYPE product_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'DISCONTINUED'
);

CREATE TYPE unit_measure AS ENUM (
    'UN',
    'KIT',
    'KG',
    'G',
    'L',
    'ML',
    'M',
    'CX',
    'PR',
    'PC'
);

CREATE TYPE stock_status AS ENUM (
    'OUT_OF_STOCK',
    'LOW',
    'NORMAL',
    'OVER_STOCKED'
);

CREATE TYPE movement_type AS ENUM (
    'ENTRY',
    'EXIT',
    'TRANSFER',
    'RETURN',
    'ADJUSTMENT'
);

CREATE TYPE movement_reason AS ENUM (
    'PURCHASE',
    'RETURN_CUSTOMER',
    'WARRANTY_REPLACEMENT',
    'SALE',
    'PROMOTIONAL_GIFT',
    'INTERNAL_USE',
    'QUALITY_CHECK',
    'RETURN_SUPPLIER',
    'INVENTORY_COUNT',
    'LOSS',
    'THEFT',
    'DAMAGE',
    'EXPIRATION',
    'TRANSFER'
);

CREATE TYPE notification_type AS ENUM (
    'STOCK_LOW',
    'OUT_OF_STOCK',
    'REORDER_POINT',
    'OVERSTOCK'
);

CREATE TYPE notification_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);