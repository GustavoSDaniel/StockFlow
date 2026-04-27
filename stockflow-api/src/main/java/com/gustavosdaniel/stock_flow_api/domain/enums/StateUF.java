package com.gustavosdaniel.stock_flow_api.domain.enums;

public enum StateUF {

    AC("Acre"),
    AL("Alagoas"),
    AP("Amapá"),
    AM("Amazonas"),
    BA("Bahia"),
    CE("Ceará"),
    DF("Distrito Federal"),
    ES("Espírito Santo"),
    GO("Goiás"),
    MA("Maranhão"),
    MT("Mato Grosso"),
    MS("Mato Grosso do Sul"),
    MG("Minas Gerais"),
    PA("Pará"),
    PB("Paraíba"),
    PR("Paraná"),
    PE("Pernambuco"),
    PI("Piauí"),
    RJ("Rio de Janeiro"),
    RN("Rio Grande do Norte"),
    RS("Rio Grande do Sul"),
    RO("Rondônia"),
    RR("Roraima"),
    SC("Santa Catarina"),
    SP("São Paulo"),
    SE("Sergipe"),
    TO("Tocantins"),
    EX("Exterior");

    private final String description;

    StateUF(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static StateUF fromName(String name) {

        if (name == null) throw new IllegalArgumentException("Estado não pode ser nulo");

        try {
            return StateUF.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(

                    "Estado inválido: '" + name + "'"
            );
        }
    }
}
