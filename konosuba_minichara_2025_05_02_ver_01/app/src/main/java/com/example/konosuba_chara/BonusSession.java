package com.example.konosuba_chara;

// BonusSession.java
import java.util.ArrayList;
import java.util.List;

public class BonusSession {
    public int startGame;                // 開始回転数
    public List<BonusEntry> bonusList;  // ボーナス一覧（時系列）

    public BonusSession(int startGame) {
        this.startGame = startGame;
        this.bonusList = new ArrayList<>();
    }

    public void addBonus(BonusEntry entry) {
        bonusList.add(entry);
    }

    public int getBonusCount() {
        return bonusList.size();
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
}
