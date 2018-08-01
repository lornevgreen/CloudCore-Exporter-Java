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


    /* Methods */

    /**
     * Creates a Gson object, a JSON parser for converting JSON Strings and objects.
     *
     * @return a Gson object.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
    }

    public static StringBuilder coinsToCSV(ArrayList<CloudCoin> coins) {
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

    /**
     * Pads a String with characters appended in the beginning.
     * This is primarily used to pad 0's to hexadecimal Strings.
     *
     * @param string  the String to pad.
     * @param length  the length of the output String.
     * @param padding the character to pad the String with.
     * @return a padded String with the specified length.
     */
    public static String padString(String string, int length, char padding) {
        return String.format("%" + length + "s", string).replace(' ', padding);
    }
}
