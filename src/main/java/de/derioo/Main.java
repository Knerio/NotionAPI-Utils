package de.derioo;

import org.jline.reader.UserInterruptException;

public class Main {


    public static void main(String[] args) {
        System.out.println("Booting app");
        try {
            new Start().start();
        }catch (UserInterruptException e) {
            System.out.println("App crashed");
            return;
        }

        System.out.println("app ended");
    }
}