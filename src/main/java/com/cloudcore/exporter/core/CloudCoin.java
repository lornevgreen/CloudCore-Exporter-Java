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

    public transient String folder;

    public transient String currentFilename;


    /* Constructors */

    /**
     * Simple CloudCoin constructor for setting the filepath of the coin. This is used when deleting or renaming a file.
     *
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     */
    public CloudCoin(String folder, String filename) {
        this.folder = folder;
        this.currentFilename = filename;
    }


    /* Methods */

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

    public void setNn(int nn) { this.nn = nn; }
    public void setSn(int sn) { this.sn = sn; }
    public void setAn(ArrayList<String> an) { this.an = an; }
    public void setEd(String ed) { this.ed = ed; }
    public void setPown(String pown) { this.pown = pown; }
    public void setAoid(ArrayList<String> aoid) { this.aoid = aoid; }
}
