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
