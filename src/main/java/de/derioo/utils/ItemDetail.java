package de.derioo.utils;

import de.derioo.PropertiesType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public record ItemDetail(String name, PropertiesType type, Object value) {

    @Contract("_, _, _ -> new")
    public static @NotNull ItemDetail of(String name, PropertiesType type, Object value) {
        return new ItemDetail(name ,type, value);
    }


}
