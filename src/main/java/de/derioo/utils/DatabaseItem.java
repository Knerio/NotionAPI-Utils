package de.derioo.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.derioo.PropertiesType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Data
public class DatabaseItem {

    private final String uuid;
    private final List<ItemDetail> details = new ArrayList<>();
    private final JsonObject o;

    public String toString() {
        return o.toString();
    }

    public DatabaseItem(@NotNull JsonObject o) {
        this.uuid = o.get("id").getAsString();
        this.o = o;

        if (o.get("properties") == null) return;

        for (Map.Entry<String, JsonElement> properties : o.get("properties").getAsJsonObject()
                .entrySet()) {
            JsonObject object = properties.getValue().getAsJsonObject();
            PropertiesType type = PropertiesType.fromString(object.get("type").getAsString());
            details.add(ItemDetail.of(properties.getKey(), type, this.getValueFromElement(object, type)));
        }

    }

    public List<ItemDetail> getDetails() {
        return this.details;
    }
    public ItemDetail getDetail(String key) {
        Optional<ItemDetail> first = this.details.stream().filter(detail -> detail.name().equals(key)).findFirst();
        return first.orElse(null);
    }

    private Object getValueFromElement(JsonElement element, PropertiesType type) {
        if (element.getAsJsonObject().get(type.getType()).isJsonNull())return null;

        switch (type) {
            case NUMBER -> {
                return element.getAsDouble();
            }
            case DATE -> {
                return element.getAsJsonObject().get(type.getType()).getAsJsonObject().get("start").getAsString();
            }
            case RICH_TEXT, TITLE -> {
                JsonArray array = element.getAsJsonObject().get(type.getType()).getAsJsonArray();
                if (array.asList().size() == 0)return null;
                return array.get(0).getAsJsonObject().get("text").getAsJsonObject().get("content").getAsString();
            }
            case CHECKBOX -> {
                return element.getAsJsonObject().get(type.getType()).getAsBoolean();
            }
        }

        return null;
    }


}
