package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import com.cloudcore.exporter.core.Config;
import com.cloudcore.exporter.core.Stack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
            csv.append(CoinUtils.toCSV(coin) + System.lineSeparator());
        }
        return csv;
    }

    public static String padString(String string, int length, char padding) {
        return String.format("%" + length + "s", string).replace(' ', padding);
    }
}
