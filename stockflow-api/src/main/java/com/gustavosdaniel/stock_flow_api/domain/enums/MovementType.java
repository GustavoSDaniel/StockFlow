package com.gustavosdaniel.stock_flow_api.domain.enums;

import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;

import java.util.Collections;
import java.util.Set;

/**
 * Enumerates the types of inventory movements and their valid reasons.
 * <p>
 * Each type defines a set of allowed {@link MovementReason} values. The
 * {@link #validateReason(MovementReason)} method enforces business rules by
 * throwing a {@link BusinessRuleException} if a reason is incompatible with
 * the movement type.
 * </p>
 *
 * @see MovementReason
 */
public enum MovementType {

    ENTRY(Set.of(

            MovementReason.PURCHASE,
            MovementReason.RETURN_CUSTOMER,
            MovementReason.WARRANTY_REPLACEMENT
    )),

    EXIT(Set.of(

            MovementReason.SALE,
            MovementReason.PROMOTIONAL_GIFT,
            MovementReason.INTERNAL_USE,
            MovementReason.LOSS,
            MovementReason.THEFT,
            MovementReason.DAMAGE,
            MovementReason.EXPIRATION,
            MovementReason.RETURN_SUPPLIER

    )),

    TRANSFER(Set.of(

            MovementReason.TRANSFER
    )),

    RETURN(Set.of(

            MovementReason.RETURN_CUSTOMER,
            MovementReason.RETURN_SUPPLIER,
            MovementReason.WARRANTY_REPLACEMENT
    )),


    ADJUSTMENT(Set.of(

            MovementReason.INVENTORY_COUNT,
            MovementReason.QUALITY_CHECK
    ));

    private final Set<MovementReason> validReasons;

    MovementType(Set<MovementReason> validReason) {
        this.validReasons = validReason;
    }

    public boolean isValidReason(MovementReason reason){
        return validReasons.contains(reason);
    }

    public Set<MovementReason> getValidReason() {

        return Collections.unmodifiableSet(validReasons);
    }

    public void validateReason(MovementReason reason){

        if (!isValidReason(reason)) throw new BusinessRuleException(String.format(
                "Motivo '%s' inválido para o tipo '%s'. " +
                        "Motivos válidos: %s",
                reason, this.name(), validReasons
        ));
    }
}
