package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.*;

public class CloudCoin {


    /* JSON Fields */

    @Expose
    @SerializedName("nn")
    private int nn;
    @Expose
    @SerializedName("sn")
    private int sn;
    @Expose
    @SerializedName("an")
    private ArrayList<String> an = new ArrayList<>(Config.nodeCount);
    @Expose
    @SerializedName("ed")
    private String ed = CoinUtils.calcExpirationDate();
    @Expose
    @SerializedName("pown")
    private String pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
    @Expose
    @SerializedName("aoid")
    private ArrayList<String> aoid = new ArrayList<>();


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
            cc.getAn().add(header.substring(startAn, startAn + 32));
            startAn += 32;
        }

        cc.setAoid(null); //header.substring(808, 840);
        cc.setPown(CoinUtils.pownHexToString(header.substring(840, 872)));
        //cc.hc = header.substring(890, 898);
        cc.setEd(CoinUtils.expirationDateHexToString(header.substring(900, 902)));
        cc.setNn(Integer.valueOf(header.substring(902, 904), 16));
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

            coin.setSn(Integer.parseInt(values[0]));
            // values[1] is denomination.
            coin.setNn(Integer.parseInt(values[2]));
            ArrayList<String> ans = new ArrayList<>();
            for (int i = 0; i < Config.nodeCount; i++)
                ans.add(values[i + 3]);
            coin.setAn(ans);

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
        builder.append("cloudcoin: (nn:").append(getNn()).append(", sn:").append(getSn());
        if (null != getEd()) builder.append(", ed:").append(getEd());
        if (null != getPown()) builder.append(", pown:").append(getPown());
        if (null != getAoid()) builder.append(", aoid:").append(getAoid().toString());
        if (null != getAn()) builder.append(", an:").append(getAn().toString());

        return builder.toString();
    }


    /* Getters and Setters */

    public int getNn() { return nn; }
    public int getSn() { return sn; }
    public ArrayList<String> getAn() { return an; }
    public String getEd() { return ed; }
    public String getPown() { return pown; }
    public ArrayList<String> getAoid() { return aoid; }
    public String getFullFilePath() { return fullFilePath; }

    public void setNn(int nn) { this.nn = nn; }
    public void setSn(int sn) { this.sn = sn; }
    public void setAn(ArrayList<String> an) { this.an = an; }
    public void setEd(String ed) { this.ed = ed; }
    public void setPown(String pown) { this.pown = pown; }
    public void setAoid(ArrayList<String> aoid) { this.aoid = aoid; }

    public void setFullFilePath(String fullFilePath) { this.fullFilePath = fullFilePath; }
}
