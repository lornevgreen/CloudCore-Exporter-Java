package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
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
    public transient String currentExtension;


    /* Constructors */

    public CloudCoin() {
        an = new ArrayList<>();
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a CSV file. (Comma-Separated Values)
     *
     * @param csv
     * @return
     */
    public CloudCoin(String csv) {
        try {
            String[] values = csv.split(",");
            System.out.println(values[0]);

            sn = Integer.parseInt(values[0]);
            nn = Integer.parseInt(values[1]);
            an = new ArrayList<>();
            for (int i = 0; i < Config.NodeCount; i++) {
                an.add(values[i + 3]);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * CloudCoin Constructor for importing new coins from a JSON-encoded file.
     *
     * @param nn Network Number
     * @param sn Serial Number
     * @param an Authenticity Numbers
     * @param ed Expiration Date
     * @param pown Pown Results
     * @param aoid Array Of Idiosyncratic Data
     */
    public CloudCoin(String currentFilename, String currentExtension, int nn, int sn, ArrayList<String> an, String ed, String pown, ArrayList<String> aoid) {
        this.currentFilename = currentFilename;
        this.currentExtension = currentExtension;
        this.nn = nn;
        this.sn = sn;
        this.an = an;
        this.ed = ed;
        this.pown = pown;
        this.aoid = aoid;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cloudcoin: (nn:").append(nn).append(", sn:").append(sn);
        if (null != ed) builder.append(", ed:").append(ed);
        if (null != aoid) builder.append(", aoid:").append(aoid.toString());
        if (null != an) builder.append(", an:").append(an.toString());
        if (null != pan) builder.append(",\n pan:").append(Arrays.toString(pan));

        return builder.toString();
    }

    /* Getters and Setters */

    public int getSn() {
        return sn;
    }

    public void setSn(int sn) {
        this.sn = sn;
    }
}
