package com.example.konosuba_chara.enums;



public enum CharacterType {


    AQUA("アクア　　", "Aqua", CharacterCategory.MAIN),
    DARKNESS("ダクネス　", "Darkness", CharacterCategory.MAIN),
    MEGUMIN("めぐみん　", "Megumin", CharacterCategory.MAIN),
    YUNYUN("ゆんゆん　", "Yunyun", CharacterCategory.SUB),
    WIZ("ウィズ　　", "Wiz", CharacterCategory.SUB),
    CHRIS("クリス　　", "Chris", CharacterCategory.SUB),
    SUCCUBUS("サキュバス", "Succubus", CharacterCategory.SUCCUBUS),
    OTHERS("確定系　　", "Others", CharacterCategory.FIXED);

//    AQUA("アクア", "Aqua"),
//    DARKNESS("ダクネス", "Darkness"),
//    MEGUMIN("めぐみん", "Megumin"),
//    WIZ("ウィズ", "Wiz"),
//    YUNYUN("ゆんゆん", "Yunyun"),
//    CHRIS("クリス", "Chris"),
//    SUCCUBUS("サキュバス", "Succubus"),
//    OTHERS("確定系", "Others");

    private final String japaneseName;
    private final String englishName;
    private final CharacterCategory category;

    //    CharacterType(String japaneseName, String englishName) {
//        this.japaneseName = japaneseName;
//        this.englishName = englishName;
//    }
    CharacterType(String japaneseName, String englishName, CharacterCategory category) {
        this.japaneseName = japaneseName;
        this.englishName = englishName;
        this.category = category;
    }

    public String getJapaneseName() {
        return japaneseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public CharacterCategory getCategory() {
        return category;
    }
    @Override
    public String toString() {
        return japaneseName;
    }
}
