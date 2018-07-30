package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import com.cloudcore.exporter.core.Config;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;

import static java.time.temporal.ChronoUnit.DAYS;

public class CoinUtils {


    public static void calcExpirationDate(CloudCoin coin) {
        coin.ed = calcExpirationDate();
    }
    public static String calcExpirationDate() {
        LocalDate expirationDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        return (expirationDate.getMonthValue() + "-" + expirationDate.getYear());
    }

    public static String toCSV(CloudCoin coin) {
        StringBuilder csv = new StringBuilder(coin.getSn() + "," + coin.nn + ",");

        for (int i = 0; i < Config.NodeCount; i++)
            csv.append(coin.an.get(i)).append(",");

        return csv.substring(0, csv.length() - 1);
    }

    /**
     * Returns a denomination describing the currency value of the CloudCoin.
     *
     * @param coin CloudCoin
     * @return 1, 5, 25, 100, 250, or 0 if the CloudCoin's serial number is invalid.
     */
    public static int getDenomination(CloudCoin coin) {
        int sn = coin.getSn();
        int nom;
        if (sn < 1)
            nom = 0;
        else if ((sn < 2097153))
            nom = 1;
        else if ((sn < 4194305))
            nom = 5;
        else if ((sn < 6291457))
            nom = 25;
        else if ((sn < 14680065))
            nom = 100;
        else if ((sn < 16777217))
            nom = 250;
        else
            nom = 0;

        return nom;
    }

    /**
     * Generates a String filename for the CloudCoin based on the denomination, Network Number, and Serial Number.
     * <br>
     * <br>Example: 25.1.6123456
     *
     * @return String a filename
     */
    public static String getFilename(CloudCoin coin) {
        return CoinUtils.getDenomination(coin) + ".CloudCoin." + coin.nn + "." + coin.getSn();
    }

    /**
     * Returns a String containing a hex representation of the last pown results. The results are encoded as such:
     * <br>
     * <br>0: Unknown
     * <br>1: Pass
     * <br>2: No Response
     * <br>E: Error
     * <br>F: Fail
     *
     * @param coin the CloudCoin containing the pown results.
     * @return a hex representation of the pown results.
     */
    public static String pownStringToHex(CloudCoin coin) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, j = coin.pown.length(); i < j; i++) {
            if ('u' == coin.pown.charAt(i))
                stringBuilder.append('0');
            else if ('p' == coin.pown.charAt(i))
                stringBuilder.append('1');
            else if ('n' == coin.pown.charAt(i))
                stringBuilder.append('2');
            else if ('e' == coin.pown.charAt(i))
                stringBuilder.append('E');
            else if ('f' == coin.pown.charAt(i))
                stringBuilder.append('F');
        }

        // If length is odd, append another zero for a clean hex value.
        if (stringBuilder.length() % 2 == 1)
            stringBuilder.append('0');

        return stringBuilder.toString();
    }

    /**
     * Converts a hexadecimal pown value to String.
     *
     * @param hexString the hexadecimal pown String.
     * @return the pown String.
     */
    public static String pownHexToString(String hexString) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, j = hexString.length(); i < j; i++) {
            if ('0' == hexString.charAt(i))
                stringBuilder.append('p');
            else if ('1' == hexString.charAt(i))
                stringBuilder.append('9');
            else if ('2' == hexString.charAt(i))
                stringBuilder.append('n');
            else if ('E' == hexString.charAt(i))
                stringBuilder.append('e');
            else if ('F' == hexString.charAt(i))
                stringBuilder.append('f');
        }

        return stringBuilder.toString();
    }

    /**
     * Returns a String containing a hex representation of a new expiration date, measured in months since August 2016.
     *
     * @return a hex representation of the expiration date.
     */
    public static String expirationDateStringToHex() {
        LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        LocalDate expirationDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        int monthsAfterZero = (int) (DAYS.between(zeroDate, expirationDate) / (365.25 / 12));
        return Integer.toHexString(monthsAfterZero);
    }

    /**
     * Converts a hexadecimal expiration date to String.
     *
     * @param edHex the hexadecimal expiration date.
     * @return the expiration date String.
     */
    public static String expirationDateHexToString(String edHex) {
        long monthsAfterZero = Long.valueOf(edHex, 16);
        LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        LocalDate ed = zeroDate.plusMonths(monthsAfterZero);
        return ed.getMonthValue() + "-" + ed.getYear();
    }
}
