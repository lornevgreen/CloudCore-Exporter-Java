package com.cloudcore.exporter.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class CloudCoin {


    @Expose
    @SerializedName("nn")
    public int nn;
    @Expose
    @SerializedName("sn")
    private int sn;
    @Expose
    @SerializedName("an")
    public ArrayList<String> an;
    @Expose
    @SerializedName("ed")
    public String ed;
    @Expose
    @SerializedName("pown")
    public String pown;
    @Expose
    @SerializedName("aoid")
    public ArrayList<String> aoid;

    public transient String[] pan = new String[Config.NodeCount];
    public transient String edHex;// Months from zero date that the coin will expire.

    public transient String currentFilename;

    public transient int denomination;


    //Constructors

    public CloudCoin() {
        an = new ArrayList<>();
    }

    /**
     * CloudCoin Constructor for importing new coins from a JSON-encoded file.
     *
     * @param nn  Network Number
     * @param sn  Serial Number
     * @param ans Authenticity Numbers
     */
    public CloudCoin(int nn, int sn, String[] ans) {
        this.nn = nn;
        this.sn = sn;
        this.an = new ArrayList<>(Arrays.asList(ans));
    }

    public CloudCoin(String currentFilename, int nn, int sn, ArrayList<String> an, String ed, String pown, ArrayList<String> aoid) {
        this.currentFilename = currentFilename;
        this.nn = nn;
        this.sn = sn;
        this.an = an;
        this.ed = ed;
        this.pown = pown;
        this.aoid = aoid;

        denomination = getDenomination();
    }

    @Override
    public String toString() {
        return "cloudcoin: (nn:" + nn + ", sn:" + sn + ", ed:" + ed + ", aoid:" + aoid.toString() + ", an:" + an.toString() + ",\n pan:" + Arrays.toString(pan);
    }

    public static CloudCoin FromCSV(String csvLine) {
        try {
            CloudCoin coin = new CloudCoin();
            String[] values = csvLine.split(",");
            System.out.println(values[0]);
            coin.sn = Integer.parseInt(values[0]);
            coin.nn = Integer.parseInt(values[1]);
            coin.denomination = Integer.parseInt(values[1]);
            coin.an = new ArrayList<>();
            for (int i = 0; i < Config.NodeCount; i++) {
                coin.an.add(values[i + 3]);
            }

            return coin;

        } catch (Exception e) {

        }
        return null;
    }

    public String FileName() {
        return this.getDenomination() + ".CloudCoin." + nn + "." + sn + ".";
    }

    public String GetCSV() {
        String csv = this.getSn() + "," + this.nn + ",";


        for (int i = 0; i < Config.NodeCount; i++) {
            csv += an.get(i) + ",";
        }

        return csv.substring(0, csv.length() - 1);
    }

    public int getDenomination() {
        int nom;
        if ((sn < 1))
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


    public void CalcExpirationDate() {
        LocalDate expirationnnnDate = LocalDate.now().plusYears(Config.YEARSTILEXPIRE);
        ed = (expirationnnnDate.getMonth() + "-" + expirationnnnDate.getYear());

        LocalDate zeroDateee = LocalDate.of(2016, 8, 13);
        int monthsAfterZero = (int) (DAYS.between(expirationnnnDate, zeroDateee) / (365.25 / 12));
        this.edHex = String.format("0x%08X", monthsAfterZero);
    }


    public int getSn() {
        return sn;
    }
    public void setSn(int sn) {
        this.sn = sn;
        denomination = getDenomination();
    }


    public enum DetectionStatus {
    }

}
