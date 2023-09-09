package de.derioo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.derioo.annotations.Async;
import de.derioo.annotations.CanBeExecutedFromConsole;
import de.derioo.utils.Config;
import de.derioo.utils.DatabaseItem;
import de.derioo.utils.GetRequest;
import de.derioo.utils.ItemDetail;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;

public class Start {

    private final String[] commands = new String[] {"execute {method}"};


    private String notionToken;
    private String databaseID;

    private NotionAPI notionAPI;


    private JsonObject config;
    
    private final LineReader reader = LineReaderBuilder.builder().build();

    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        this.reader.setAutosuggestion(LineReader.SuggestionType.HISTORY);
        
        this.config = new Config(new File(".", "config.json")).getContent();
        this.notionToken = this.config.get("notion").getAsJsonObject().get("Internal-Integration-Secret").getAsString();

        this.databaseID = this.getDatabaseID();

        this.notionAPI = new NotionAPI(this.notionToken, this.databaseID);

        this.notionAPI.execute("checkForDelete", this);

        


        while (true) {
            String command = this.reader.readLine("> ");

            if (command.startsWith("execute ")) {
                String[] commandArgsSplit = command.replace("execute ", "").split("-");

                String methodName = commandArgsSplit[0];


                Optional<Method> optionalMethod = Arrays.stream(this.getClass().getDeclaredMethods()).filter(method -> method.getName().equals(methodName) && method.isAnnotationPresent(CanBeExecutedFromConsole.class))
                        .findFirst();


                if (optionalMethod.isEmpty()) {
                    System.out.println("'" + methodName + "' doesn't exists");
                    continue;
                }

                CanBeExecutedFromConsole annotation = optionalMethod.get().getAnnotation(CanBeExecutedFromConsole.class);

                System.out.println("You need " + annotation.argsLenght() + " arguments");

                String[] args = new String[annotation.argsLenght()];

                for (int i = 0; i < annotation.argsLenght(); i++) {
                    System.out.println("Please add one argument and press enter (Argument name = '" + annotation.argsName().split(" ")[i].replace("-", " ") + "'):");
                    System.out.print("> ");
                    args[i] = this.scanner.nextLine();
                    int argsLeft = annotation.argsLenght() - (i + 1);
                    if (argsLeft != 0) System.out.println("You need " + argsLeft + " more");
                }

                if (optionalMethod.get().isAnnotationPresent(Async.class)) {
                    new Thread(() -> {
                        System.out.println(this.notionAPI.invoke(optionalMethod.get(), this, (Object) args));
                        System.out.print("> ");
                    }).start();
                } else {
                    System.out.println(this.notionAPI.invoke(optionalMethod.get(), this, (Object) args));
                }
            } else {
                System.out.println("Please use one of the following commands:");
                System.out.println();
                for (String s : this.commands) {
                    System.out.println("- " + s);
                }
                System.out.println();
            }
        }

    }

    private String getDatabaseID() {
        Map<String, String> names = new HashMap<>();

        System.out.println("Available Databases:");
        for (JsonElement e : this.config.get("notion").getAsJsonObject().get("database-ids").getAsJsonArray().asList()) {
            System.out.print("- " + e.getAsString() + " (");
            String name = NotionAPI.getDetails(e.getAsString(), this.notionToken).getAsJsonObject().get("title").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsJsonObject().get("content").getAsString();
            names.put(name, NotionAPI.getDetails(e.getAsString(), this.notionToken).getAsJsonObject().get("id").getAsString());
            System.out.print(name + ")");
            System.out.println();
        }

        System.out.println();

        System.out.println("Wich database do you want?");


        while (true) {
            String nextLine = this.reader.readLine("> ");
            Optional<String> first = names.keySet().stream().filter(s -> s.equalsIgnoreCase(nextLine)).findFirst();
            if (first.isPresent()) {
                System.out.println("Picked: " + names.get(first.get()) + " (" + first.get() + ")");

                System.out.println("Started app");
                return names.get(first.get());
            }
            System.out.println(nextLine + " doesn't exists");
        }


    }


    public static @NotNull String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return currentDate.format(formatter);
    }

    @CanBeExecutedFromConsole(argsLenght = 3, argsName = "Name Descrption Status-(Leave-blank-for-default)")
    @Async
    public String addObject(String[] args) {
        if (!statusExists(args[2]) && !Objects.equals(args[2], "")) return "Status '"+args[2]+"' doesnt exists";
        try {
            this.notionAPI.addItem(ItemDetail.of("Name", PropertiesType.TITLE, args[0]), ItemDetail.of("Status", PropertiesType.STATUS, "Not started"), ItemDetail.of("Descrption", PropertiesType.RICH_TEXT, args[1]), ItemDetail.of("Status", PropertiesType.STATUS, args[2].equals("") ? "Not started" : args[2])).get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage());
        }

        return "executed";
    }

    private boolean statusExists(String arg) {
        GetRequest request = new GetRequest("https://api.notion.com/v1/databases/"+this.databaseID);

        request.addHeader("Notion-version", NotionAPI.NOTION_VERSION);

        for (JsonElement element : JsonParser.parseString(request.execute(this.notionToken)).getAsJsonObject().get("properties").getAsJsonObject().get("Status").getAsJsonObject().get("status").getAsJsonObject().get("options")
                .getAsJsonArray()) {
            if (element.getAsJsonObject().get("name").getAsString().equals(arg)) return true;
        }
        return false;
    }


    @Async
    public void checkForDelete() {
        List<String> skips = new ArrayList<>();
        while (true) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                for (DatabaseItem databaseItem : this.notionAPI.getItemsfromDB().get()) {
                    if (skips.contains(databaseItem.getUuid())) continue;
                    if (!databaseItem.getDetail("Status").value().toString().equals("Delete")) {
                        continue;
                    }

                    skips.add(databaseItem.getUuid());

                    new Thread(() -> {
                        try {
                            int seconds = 10;
                            DatabaseItem currentItem;

                            String caption = "{sec}";

                            currentItem =
                                    this.notionAPI.modifyItem(databaseItem,
                                                    ItemDetail.of("Deletion",
                                                            PropertiesType.RICH_TEXT,
                                                            caption.replace("{sec}", 10 + "")))
                                            .get();

                            while (seconds != 1) {
                                Thread.sleep(500);

                                if (currentItem == null) break;

                                currentItem = this.notionAPI.modifyItem(currentItem,
                                        ItemDetail.of("Deletion",
                                                PropertiesType.RICH_TEXT,
                                                currentItem.getDetail("Deletion").value().toString()
                                                        .replace(
                                                                String.valueOf(seconds),
                                                                String.valueOf(seconds - 1))
                                        )).get();

                                if (!currentItem.getDetail("Status").value().toString().equals("Delete")) {
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
                            if (currentItem == null) return;
                            skips.remove(currentItem.getUuid());

                            if (currentItem.getDetail("Status").value().toString().equals("Delete")) {
                                this.notionAPI.deleteItem(currentItem).get();
                            }


                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }

                    }).start();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
