package com.example.konosuba_chara;

// BonusEntry.java
public class BonusEntry {
    public int game;               // ゲーム数（回転数）
    public String trigger;        // 契機（手入力 or 選択）
    public String triggerCount;   // 契機の回数（手入力）
    public String bonusType;      // ボーナスの種類（選択）
    public String note;           // 備考

    public BonusEntry(int game, String trigger, String triggerCount, String bonusType, String note) {
        this.game = game;
        this.trigger = trigger;
        this.triggerCount = triggerCount;
        this.bonusType = bonusType;
        this.note = note;
    }

    // オプション：整形して表示や書き出し用のメソッド
    public String format() {
        return String.format("%-5d  %-6s  %-6s  %-20s  %s",
                game,
                trigger == null ? "" : trigger,
                triggerCount == null ? "" : triggerCount,
                bonusType == null ? "" : bonusType,
                note == null ? "" : note);
    }
}


