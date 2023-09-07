package de.derioo.utils;

import java.io.*;

public class FileUtils {


    public static void writeToFile(File file, Object content){
        try {
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(content.toString());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(File file, Object... content){
        try {
            FileWriter myWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(myWriter);
            for (Object s : content) {
                writer.write(s.toString());
                writer.newLine();
            }
            writer.close();
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(File file){
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resultStringBuilder.toString();
    }

}
