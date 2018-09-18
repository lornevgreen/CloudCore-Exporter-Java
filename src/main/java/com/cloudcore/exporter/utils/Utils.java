package com.cloudcore.exporter.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

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
     * Converts a byte array to a hexadecimal String.
     *
     * @param data the byte array.
     * @return a hexadecimal String.
     */
    public static String bytesToHexString(byte[] data) {
        final String HexChart = "0123456789ABCDEF";
        final StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(HexChart.charAt((b & 0xF0) >> 4)).append(HexChart.charAt((b & 0x0F)));
        return hex.toString();
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
