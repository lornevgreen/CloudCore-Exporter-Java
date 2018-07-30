package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.*;

public class CloudCoin {


    @Expose
    @SerializedName("nn")
    public int nn;
    @Expose
    @SerializedName("sn")
    private int sn;
    @Expose
    @SerializedName("an")
    public ArrayList<String> an = new ArrayList<>();
    @Expose
    @SerializedName("ed")
    public String ed = CoinUtils.calcExpirationDate();
    @Expose
    @SerializedName("pown")
    public String pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
    @Expose
    @SerializedName("aoid")
    public ArrayList<String> aoid = new ArrayList<>();

    public transient String currentFilename;
    public transient String currentExtension;


    /* Constructors */

    public CloudCoin() {
        an = new ArrayList<>();
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a CSV file. (Comma-Separated Values)
     *
     * @param csv csv String
     * @param filename Filename
     * @return
     */
    public CloudCoin(String csv, String filename) {
        currentExtension = filename.substring(filename.lastIndexOf('.'));
        currentFilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1,
                filename.length() - currentExtension.length());

        try {
            String[] values = csv.split(",");

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
     * @param filename Filename
     * @param nn Network Number
     * @param sn Serial Number
     * @param an Authenticity Numbers
     * @param ed Expiration Date
     * @param pown Pown Results
     * @param aoid Array Of Idiosyncratic Data
     */
    public CloudCoin(String filename, int nn, int sn, ArrayList<String> an, String ed, String pown, ArrayList<String> aoid) {
        currentExtension = filename.substring(filename.lastIndexOf('.'));
        currentFilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1,
                filename.length() - currentExtension.length());

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
