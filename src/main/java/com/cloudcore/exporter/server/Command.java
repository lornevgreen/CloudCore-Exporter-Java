package com.cloudcore.exporter.server;

import com.cloudcore.exporter.core.Config;
import com.cloudcore.exporter.utils.CoinUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Command {


    /* JSON Fields */

    @Expose
    @SerializedName("command")
    public String command;
    @Expose
    @SerializedName("account")
    public String account;
    @Expose
    @SerializedName("amount")
    public int amount;
    /**
     * 0, 1, 2, 3: multiple notes, stack, jpegs, or CSV
     */
    @Expose
    @SerializedName("type")
    public int type;
    @Expose
    @SerializedName("tag")
    public String tag = "";

    public String filename;
}
