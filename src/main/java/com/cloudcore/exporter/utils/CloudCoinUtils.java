package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import com.cloudcore.exporter.core.Config;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class CloudCoinUtils {


    public void CalcExpirationDate(CloudCoin coin) {
        LocalDate expirationnnnDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        coin.ed = (expirationnnnDate.getMonth() + "-" + expirationnnnDate.getYear());

        LocalDate zeroDateee = LocalDate.of(2016, 8, 13);
        int monthsAfterZero = (int) (DAYS.between(expirationnnnDate, zeroDateee) / (365.25 / 12));
        coin.edHex = String.format("0x%08X", monthsAfterZero);
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
    public static String pownToHex(CloudCoin coin) {
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

        // If length is odd, append another number for a clean hex value.
        if (stringBuilder.length() % 2 == 1)
            stringBuilder.append('0');

        return stringBuilder.toString();
    }

    /**
     * Returns a String containing a hex representation of a new expiration date, measured in months since August 2016.
     *
     * @return a hex representation of the expiration date.
     */
    public static String expirationDateToHex() {
        LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        LocalDate expirationDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        int monthsAfterZero = (int) (DAYS.between(zeroDate, expirationDate) / (365.25 / 12));
        System.out.println("expiration months: " + monthsAfterZero);
        return Integer.toHexString(monthsAfterZero);
    }
}
