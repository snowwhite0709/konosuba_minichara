package com.example.konosuba_chara_vol_01;

public enum CharacterType {
    AQUA("アクア", "Aqua"),
    DARKNESS("ダクネス", "Darkness"),
    MEGUMIN("めぐみん", "Megumin"),
    WIZ("ウィズ", "Wiz"),
    YUNYUN("ゆんゆん", "Yunyun"),
    CHRIS("クリス", "Chris"),
    SUCCUBUS("サキュバス", "Succubus"),
    OTHERS("確定系", "Others");

    private final String japaneseName;
    private final String englishName;

    CharacterType(String japaneseName, String englishName) {
        this.japaneseName = japaneseName;
        this.englishName = englishName;
    }

    public String getJapaneseName() {
        return japaneseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    @Override
    public String toString() {
        return japaneseName;
    }
}
