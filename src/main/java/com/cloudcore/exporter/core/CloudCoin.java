package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.*;

public class CloudCoin {


    /* JSON Fields */

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


    /* Fields */

    private transient String fullFilePath;


    /* Constructors */

    /**
     * Simple CloudCoin constructor for setting the filepath of the coin. This is used when deleting or renaming a file.
     *
     * @param fullFilePath the absolute filepath of the CloudCoin.
     */
    private CloudCoin(String fullFilePath) {
        this.fullFilePath = fullFilePath;
    }


    /* Methods */

    /**
     * CloudCoin Constructor for importing a CloudCoin from a JPG file.
     *
     * @param header       JPG header string.
     * @param fullFilePath the absolute filepath of the CloudCoin.
     * @return a CloudCoin object.
     */
    public static CloudCoin fromJpgHeader(String header, String fullFilePath) {
        CloudCoin cc = new CloudCoin(fullFilePath);

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
     * CloudCoin Constructor for importing a CloudCoin from a CSV file.
     *
     * @param csv          CSV file as a String.
     * @param fullFilePath the absolute filepath of the CloudCoin.
     * @return a CloudCoin object.
     */
    public static CloudCoin fromCsv(String csv, String fullFilePath) {
        CloudCoin coin = new CloudCoin(fullFilePath);

        try {
            String[] values = csv.split(",");

            coin.sn = Integer.parseInt(values[0]);
            // values[1] is denomination.
            coin.nn = Integer.parseInt(values[2]);
            coin.an = new ArrayList<>();
            for (int i = 0; i < Config.nodeCount; i++)
                coin.an.add(values[i + 3]);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        return coin;
    }

    /**
     * Returns a human readable String describing the contents of the CloudCoin.
     *
     * @return a String describing the CloudCoin.
     */
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

    public void setFullFilePath(String fullFilePath) {
        this.fullFilePath = fullFilePath;
    }

    public String getFullFilePath() {
        return fullFilePath;
    }
}
