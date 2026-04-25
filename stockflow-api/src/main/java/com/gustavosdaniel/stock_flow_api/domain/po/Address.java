package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("address")
public class Address extends BaseEntity{

    @Id
    private UUID id;


}
