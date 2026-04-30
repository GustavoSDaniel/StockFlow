package com.gustavosdaniel.stock_flow_api.domain.enums;

public enum UnitMeasure {

    UN("UNIDADE"),
    KIT("KIT"),
    KG("QUILOGRAMA"),
    G("GRAMA"),
    L("LITRO"),
    ML("MILILITRO"),
    M("METRO"),
    CX("CAIXA"),
    PR("PAR"),
    PC("PEÇA");

    private final String name;

    UnitMeasure(String description) {
        this.name = description;
    }

    public String getName() {
        return name;
    }

    public static UnitMeasure fromName(String name) {
        if (name == null) throw new IllegalArgumentException(
                "UnitMeasure não pode ser nulo"
        );
        try {
            return UnitMeasure.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "UnitMeasure inválido: '" + name + "'"
            );
        }
    }
}
