package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import com.cloudcore.exporter.core.Config;
import com.cloudcore.exporter.core.Stack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class Utils {


    public static Gson createGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    public static CloudCoin[] LoadJson(String filename) {
        try {
            byte[] json = Files.readAllBytes(Paths.get(filename));
            Gson gson = createGson();
            Stack coins = gson.fromJson(new String(json), Stack.class);
            return coins.cc;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static StringBuilder CoinsToCSV(ArrayList<CloudCoin> coins) {
        StringBuilder csv = new StringBuilder();

        String headerLine = String.format("sn,denomination,nn,");
        String headeranString = "";
        for (int i = 0; i < Config.NodeCount; i++) {
            headeranString += "an" + (i + 1) + ",";
        }

        // Write the Header Record
        csv.append(headerLine + headeranString + System.lineSeparator());

        // Write the Coin Serial Numbers
        for (CloudCoin coin : coins) {
            csv.append(coin.GetCSV() + System.lineSeparator());
        }
        return csv;
    }


    private static Random random = new Random();
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String RandomString(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    public static String padString(String string, int length, char padding) {
        return String.format("%" + length + "s", string).replace(' ', padding);
    }


    public static int tryParseInteger(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int[] combine(int[] a, int[] b){
        int length = a.length + b.length;
        int[] array = new int[length];
        System.arraycopy(a, 0, array, 0, a.length);
        System.arraycopy(b, 0, array, a.length, b.length);
        return array;
    }
}
