package com.pm.server.datatype;

public class EatenDotsReport {

    private Integer eatenPacdots;

    private Integer eatenPowerdots;

    public EatenDotsReport() {
        eatenPacdots = 0;
        eatenPowerdots = 0;
    }

    public Integer getEatenPacdots() {
        return eatenPacdots;
    }

    public void addEatenPacdot() {
        eatenPacdots++;
    }

    public Integer getEatenPowerdots() {
        return eatenPowerdots;
    }

    public void addEatenPowerdot() {
        eatenPowerdots++;
    }

}
