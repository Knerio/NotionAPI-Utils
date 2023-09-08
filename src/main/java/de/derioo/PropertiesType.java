package de.derioo;

import lombok.Getter;

public enum PropertiesType {

    NUMBER("number"),
    CHECKBOX("checkbox"),
    DATE("date"),

    TITLE("title"),

    STATUS("status"),

    RICH_TEXT("rich_text");

    @Getter
    private final String type;

    PropertiesType(String type) {
        this.type = type;
    }

    public static PropertiesType fromString(String s) {
        for (PropertiesType value : PropertiesType.values()) {
            if (value.name().equalsIgnoreCase(s.toUpperCase()) || value.getType().equalsIgnoreCase(s)) return value;
        }
        return null;
    }


}
