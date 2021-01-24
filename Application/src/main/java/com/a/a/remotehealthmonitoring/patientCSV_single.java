package com.a.a.remotehealthmonitoring;

public class patientCSV_single {
    private String date;
    private String time;
    private String ecg;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getECG() {
        return ecg;
    }

    public void setECG(String ecg) {
        this.ecg = ecg;
    }

    @Override
    public String toString() {
        return "{" + date + "::" + time + "::" + ecg + "}";
    }
}
