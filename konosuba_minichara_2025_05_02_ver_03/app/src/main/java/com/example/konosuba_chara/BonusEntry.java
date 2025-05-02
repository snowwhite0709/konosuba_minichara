package com.example.konosuba_chara;

// BonusEntry.java
public class BonusEntry {
    private Integer game;               // ゲーム数（回転数）
    private String trigger;        // 契機（手入力 or 選択）
    private String triggerCount;   // 契機の回数（手入力）
    private String bonusType;      // ボーナスの種類（選択）
    private String note;           // 備考

    // ✅ デフォルトコンストラクタ
    public BonusEntry() {
        // 初期値を必要に応じて設定してもOK
        this.game = 0;
        this.trigger = "";
        this.triggerCount = "";
        this.bonusType = "";
        this.note = "";
    }

    public BonusEntry(int game, String trigger, String triggerCount, String bonusType, String note) {
        this.game = game;
        this.trigger = trigger;
        this.triggerCount = triggerCount;
        this.bonusType = bonusType;
        this.note = note;
    }

    public void setGame(int game) {
        this.game = game;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public void setTriggerCount(String triggerCount) {
        this.triggerCount = triggerCount;
    }

    public void setBonusType(String bonusType) {
        this.bonusType = bonusType;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getGame() {
        return game;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getTriggerCount() {
        return triggerCount;
    }

    public String getBonusType() {
        return bonusType;
    }

    public String getNote() {
        return note;
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


    @Override
    public String toString() {
        return "BonusEntry{" +
                "game=" + game +
                ", trigger='" + trigger + '\'' +
                ", triggerCount='" + triggerCount + '\'' +
                ", bonusType='" + bonusType + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}


