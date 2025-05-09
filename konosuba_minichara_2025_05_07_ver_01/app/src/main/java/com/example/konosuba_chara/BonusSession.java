package com.example.konosuba_chara;

// BonusSession.java
import java.util.ArrayList;
import java.util.List;
public class BonusSession {

    private int startGame;
    private List<BonusEntry> bonusList;

    // ✅ デフォルトコンストラクタ（使いやすさ向上）
    public BonusSession() {
        this.startGame = 0;
        this.bonusList = new ArrayList<>();
    }

    // すでにある引数ありのコンストラクタも残してOK
    public BonusSession(int startGame) {
        this.startGame = startGame;
        this.bonusList = new ArrayList<>();
    }

    // ゲッター・セッター
    public int getStartGame() {
        return startGame;
    }

    public void setStartGame(int startGame) {
        this.startGame = startGame;
    }

    public List<BonusEntry> getBonusList() {
        return bonusList;
    }

    public void setBonusList(List<BonusEntry> bonusList) {
        this.bonusList = bonusList;
    }


    // ファイル書き出し用のヘッダー付き整形
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("【A＋このすば】\n");
        sb.append("開始ゲーム数：").append(startGame).append("\n\n");
        sb.append("No.   ゲーム数  契機     契機回数  ボーナス種別             備考\n");

        int no = 1;
        for (BonusEntry entry : bonusList) {
            sb.append(String.format("%-5d %s\n", no++, entry.format()));
        }
        return sb.toString();
    }


    // ✅ ボーナスを追加
    public void addEntry(BonusEntry entry) {
        bonusList.add(entry);
    }


}
