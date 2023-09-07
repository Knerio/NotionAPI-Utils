package de.derioo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.derioo.utils.Config;
import de.derioo.utils.DatabaseItem;
import de.derioo.utils.ItemDetail;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Start {


    private String notionToken;
    private String databaseID;

    private NotionAPI notionAPI;


    private JsonObject config;

    public void start() {
        this.config = new Config(new File(".", "config.json")).getContent();
        this.notionToken = this.config.get("notion").getAsJsonObject().get("Internal-Integration-Secret").getAsString();
        this.databaseID = this.config.get("notion").getAsJsonObject().get("database-id").getAsString();
        this.notionAPI = new NotionAPI(this.notionToken, this.databaseID);


        //this.notionAPI.deleteAllItems();


        List<String> skips = new ArrayList<>();

        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                for (DatabaseItem databaseItem : this.notionAPI.getItemsfromDB().get()) {
                    if (skips.contains(databaseItem.getUuid())) continue;
                    if (Boolean.parseBoolean(databaseItem.getDetail("geschafft").value().toString())) {
                        skips.add(databaseItem.getUuid());
                        new Thread(() -> {
                            try {
                                int seconds = 10;
                                DatabaseItem currentItem;

                                String caption = "{sec} seconds";

                                currentItem =
                                        this.notionAPI.modifyItem(databaseItem,
                                                        ItemDetail.of("Deletion",
                                                                PropertiesType.RICH_TEXT,
                                                                        caption.replace("{sec}", 10 + "")))
                                                .get();

                                while (seconds != 1) {
                                    Thread.sleep(500);

                                    currentItem = this.notionAPI.modifyItem(currentItem,
                                            ItemDetail.of("Deletion",
                                                    PropertiesType.RICH_TEXT,
                                                    currentItem.getDetail("Deletion").value().toString()
                                                            .replace(
                                                                    String.valueOf(seconds),
                                                                    String.valueOf(seconds - 1))
                                            )).get();

                                    if (!Boolean.parseBoolean(currentItem.getDetail("geschafft").value().toString())) {
                                        currentItem = this.notionAPI.modifyItem(currentItem,
                                                ItemDetail.of("Deletion",
                                                        PropertiesType.RICH_TEXT,
                                                        currentItem.getDetail("Deletion").value().toString()
                                                                .replace(
                                                                        caption.replace("{sec}", String.valueOf(seconds - 1)),
                                                                        ""
                                                                )
                                                )).get();
                                        break;
                                    }


                                    seconds--;
                                }

                                skips.remove(currentItem.getUuid());

                                if (Boolean.parseBoolean(currentItem.getDetail("geschafft").value().toString())) {
                                    this.notionAPI.deleteItem(currentItem).get();
                                }


                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }

                        }).start();
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

//        try {
//           // this.notionAPI.addItem(ItemDetail.of("Name", PropertiesType.TITLE, "Iwas")).get();
//            for (DatabaseItem databaseItem : this.notionAPI.getItemsfromDB().get()) {
//
//                boolean geschafft = Boolean.parseBoolean(databaseItem.getDetail("geschafft").value().toString());
//                if (geschafft) {
//                    this.notionAPI.deleteItem(databaseItem).get();
//                }
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
    }


    public static String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return currentDate.format(formatter);
    }


}
