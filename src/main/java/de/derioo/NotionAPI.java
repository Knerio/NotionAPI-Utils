package de.derioo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derioo.utils.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@AllArgsConstructor
public class NotionAPI {

    public static final String NOTION_VERSION = "2022-06-28";

    private final String secret;
    private final String databaseID;

    public CompletableFuture<DatabaseItem> addItem(ItemDetail... itemDetails) {
            return CompletableFuture.supplyAsync(() -> {
                String url = "https://api.notion.com/v1/pages";

                JsonObject body = new JsonObject();

                JsonObject parent = new JsonObject();

                parent.addProperty("type", "database_id");
                parent.addProperty("database_id", this.databaseID);


                JsonObject properties = new JsonObject();

                for (ItemDetail itemDetail : itemDetails) {
                    properties.add(itemDetail.name(), this.getPropertiesObject(itemDetail.type(), itemDetail.value()));
                }


                body.add("parent", parent);
                body.add("properties", properties);

                String authorizationHeader = "Bearer " + this.secret;


                PostRequest postRequest = new PostRequest(url, body.toString());
                postRequest.addHeader("Authorization", authorizationHeader);
                postRequest.addHeader("Content-Type", "application/json");
                postRequest.addHeader("Notion-Version", NOTION_VERSION);

                String response = postRequest.execute(this.secret);

                return new DatabaseItem(JsonParser.parseString(response).getAsJsonObject());
            });

    }

    public CompletableFuture<List<DatabaseItem>> getItemsfromDB() {
        return CompletableFuture.supplyAsync(() -> {
            String url = "https://api.notion.com/v1/databases/"+ this.databaseID+"/query";

            PostRequest request = new PostRequest(url, "");

            request.addHeader("Notion-version", NOTION_VERSION);

            String execute = request.execute(this.secret);


            return JsonParser.parseString(execute).getAsJsonObject().get("results").getAsJsonArray()
                    .asList().stream().map(e -> new DatabaseItem(e.getAsJsonObject())).collect(Collectors.toList());
        });

    }

    public CompletableFuture<DatabaseItem> modifyItem(DatabaseItem item, ItemDetail... details) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject requestBody = new JsonObject();
            JsonObject properties = new JsonObject();

            for (ItemDetail detail : details) {
                properties.add(detail.name(), this.getPropertiesObject(detail.type(), detail.value()));
            }


            requestBody.add("properties", properties);



            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.notion.com/v1/pages/" + item.getUuid()))
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Notion-Version", NOTION_VERSION)
                    .header("Authorization", "Bearer " + this.secret)
                    .build();

            HttpClient httpClient = HttpClient.newHttpClient();


            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


                return this.getItemsfromDB().get().stream().filter(i -> i.getUuid().equals(item.getUuid())).findFirst().get();
            } catch (IOException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @SneakyThrows
    public void deleteAllItems() {
        for (DatabaseItem databaseItem : this.getItemsfromDB().get()) {
            this.deleteItem(databaseItem);
        }
    }

    public CompletableFuture<DatabaseItem> deleteItem(@NotNull DatabaseItem item) {
        return CompletableFuture.supplyAsync(() -> {
            DeleteRequest request = new DeleteRequest("https://api.notion.com/v1/blocks/"+ item.getUuid(), "");
            request.addHeaders("Notion-version", NOTION_VERSION);
            return new DatabaseItem(JsonParser.parseString(request.execute(this.secret)).getAsJsonObject());
        });
    }

    private @NotNull JsonObject getPropertiesObject(@NotNull PropertiesType type, Object value) {
        JsonObject innerObject;
        switch (type) {
            case NUMBER:
                innerObject = new JsonObject();

                innerObject.addProperty("type", type.getType());
                innerObject.addProperty(type.getType(), value.toString());
                return innerObject;
            case CHECKBOX:
                innerObject = new JsonObject();

                innerObject.addProperty("type", type.getType());
                innerObject.addProperty(type.getType(), Boolean.parseBoolean(value.toString()));
                return innerObject;
            case DATE:
                JsonObject object = this.getPropertiesObject(PropertiesType.NUMBER, value);
                JsonObject dateObject = new JsonObject();

                object.remove("number");
                object.add("date", dateObject);
                object.addProperty("type", type.getType());


                dateObject.addProperty("start", value.toString());



                return object;
            case RICH_TEXT:
                innerObject = new JsonObject();
                JsonArray array = new JsonArray();
                JsonObject contentObject = new JsonObject();
                JsonObject textObject = new JsonObject();



                innerObject.addProperty("type", type.getType());
                innerObject.add(type.getType(), array);

                textObject.addProperty("type", "text");
                textObject.add("text", contentObject);

                contentObject.addProperty("content", value.toString());

                array.add(textObject);


                return innerObject;
            case TITLE:
                innerObject = new JsonObject();
                array = new JsonArray();
                contentObject = new JsonObject();
                object = new JsonObject();

                object.addProperty("type", type.getType());
                object.add(type.getType(), array);

                array.add(innerObject);

                innerObject.addProperty("type", "text");
                innerObject.add("text", contentObject);

                contentObject.addProperty("content", value.toString());


                return object;
        }
        return new JsonObject();
    }



}
