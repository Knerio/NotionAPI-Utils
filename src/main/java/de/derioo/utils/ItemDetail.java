package de.derioo.utils;

import de.derioo.PropertiesType;
import de.derioo.objects.CustomObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public record ItemDetail(String name, PropertiesType type, CustomObject value) {

    @Contract("_, _, _ -> new")
    public static @NotNull ItemDetail of(String name, PropertiesType type, CustomObject value) {
        return new ItemDetail(name ,type, value);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull ItemDetail of(String name, PropertiesType type, Object value) {
        return new ItemDetail(name ,type, CustomObject.of(value));
    }


}
