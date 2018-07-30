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

    private CloudCoin(String filename) {
        setFilename(filename);
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a CSV file. (Comma-Separated Values)
     *
     * @param header JPG header string.
     * @param filename Filename
     * @return CloudCoin
     */
    public static CloudCoin fromJpgHeader(String header, String filename) {
        CloudCoin cc = new CloudCoin(filename);

        int startAn = 40;
        for (int i = 0; i < 25; i++) {
            cc.an.add(header.substring(startAn, startAn + 32));
            startAn += 32;
        }

        cc.aoid = null; //header.substring(808, 840);
        cc.pown = CoinUtils.pownHexToString(header.substring(840, 872));
        //cc.hc = header.substring(890, 898);
        cc.ed = CoinUtils.expirationDateHexToString(header.substring(900, 902));
        cc.nn = Integer.valueOf(header.substring(902, 904), 16);
        cc.setSn(Integer.valueOf(header.substring(904, 910), 16));

        return cc;
    }

    /**
     * CloudCoin Constructor for importing a CloudCoin from a CSV file. (Comma-Separated Values)
     *
     * @param csv csv String
     * @param filename Filename
     * @return CloudCoin
     */
    public static CloudCoin fromCsv(String csv, String filename) {
        CloudCoin coin = new CloudCoin(filename);

        coin.currentExtension = filename.substring(filename.lastIndexOf('.'));
        coin.currentFilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1,
                filename.length() - coin.currentExtension.length());

        try {
            String[] values = csv.split(",");

            coin.sn = Integer.parseInt(values[0]);
            coin.nn = Integer.parseInt(values[1]);
            coin.an = new ArrayList<>();
            for (int i = 0; i < Config.NodeCount; i++) {
                coin.an.add(values[i + 3]);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        return coin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cloudcoin: (nn:").append(nn).append(", sn:").append(sn);
        if (null != ed) builder.append(", ed:").append(ed);
        if (null != pown) builder.append(", pown:").append(pown);
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

    public void setFilename(String filename) {
        currentExtension = filename.substring(filename.lastIndexOf('.'));
        currentFilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1,
                filename.length() - currentExtension.length());
    }
}
