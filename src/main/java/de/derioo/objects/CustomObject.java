package de.derioo.objects;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomObject {

    private final Object o;

    public static CustomObject of(Object o) {
        return new CustomObject(o);
    }

    public boolean getAsBoolean() {
        return Boolean.parseBoolean(this.o.toString());
    }


    public String toString() {
        return o.toString();
    }

    public int getAsInt() {
        try {
            return Integer.parseInt(o.toString());
        }catch (NumberFormatException e) {
        }
        return -1;
    }


}
