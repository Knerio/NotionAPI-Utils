package de.derioo.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;

public class Config {

    private final File file;

    public Config(File file) {
        this.file = file;
    }

    public JsonObject getContent() {
        return JsonParser.parseString(FileUtils.readFile(this.file)).getAsJsonObject();
    }

}
