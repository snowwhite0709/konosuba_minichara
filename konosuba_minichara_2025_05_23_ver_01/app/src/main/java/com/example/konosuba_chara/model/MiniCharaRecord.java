package com.example.konosuba_chara.model;

import java.util.Map;

public class MiniCharaRecord {
    private String fileName;  // ファイル名
    private int bigNumber;                 // BIG回数（例: 1回目）
    private String dateTime;              // 記録日時（例: 2025-05-21T18:30:00）
    private Map<String, Integer> charaCounts;   // 各キャラの出現回数
    private Map<String, Double> charaRates;     // 各キャラの出現率（%）
    private Map<String, Integer> categoryCounts; // 各カテゴリ（メインなど）の回数
    private Map<String, Double> categoryRates;   // 各カテゴリの出現率（%）

    // コンストラクタ、getter/setterは必要に応じて


    public MiniCharaRecord(String fileName, int bigNumber, String dateTime, Map<String, Integer> charaCounts, Map<String, Double> charaRates, Map<String, Integer> categoryCounts, Map<String, Double> categoryRates) {
        this.fileName = fileName;
        this.bigNumber = bigNumber;
        this.dateTime = dateTime;
        this.charaCounts = charaCounts;
        this.charaRates = charaRates;
        this.categoryCounts = categoryCounts;
        this.categoryRates = categoryRates;
    }


    public String getFileName() {
        return fileName;
    }

    public int getBigNumber() {
        return bigNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Map<String, Integer> getCharaCounts() {
        return charaCounts;
    }

    public Map<String, Double> getCharaRates() {
        return charaRates;
    }

    public Map<String, Integer> getCategoryCounts() {
        return categoryCounts;
    }

    public Map<String, Double> getCategoryRates() {
        return categoryRates;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBigNumber(int bigNumber) {
        this.bigNumber = bigNumber;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setCharaCounts(Map<String, Integer> charaCounts) {
        this.charaCounts = charaCounts;
    }

    public void setCharaRates(Map<String, Double> charaRates) {
        this.charaRates = charaRates;
    }

    public void setCategoryCounts(Map<String, Integer> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }

    public void setCategoryRates(Map<String, Double> categoryRates) {
        this.categoryRates = categoryRates;
    }
}


