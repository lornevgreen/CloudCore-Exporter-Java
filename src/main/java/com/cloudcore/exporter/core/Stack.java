package com.cloudcore.exporter.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stack {

    @Expose
    @SerializedName("cloudcoin")
    public CloudCoin[] cc;

    public Stack(CloudCoin coin) {
        cc = new CloudCoin[1];
        cc[0] = coin;
    }

    public Stack(CloudCoin[] coins) {
        cc = coins;
    }
}
