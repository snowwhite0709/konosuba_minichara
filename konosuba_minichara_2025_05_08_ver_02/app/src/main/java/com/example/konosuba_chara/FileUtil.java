package com.example.konosuba_chara;


import android.content.Context;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileUtil {

    private static final String FILE_TITLE = "【A+このすばBIG中ぬいぐるみ】";
    private static final String FILE_EXT = ".txt";
    private static final String PREFS_NAME = "file_prefs";
    private static final String KEY_VOL = "current_vol";

    // ファイル名の日付部分（例：2025_04_30）
    public static String getCurrentDateString() {
        return new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
    }

    // 現在のファイル名を取得
    public static String getCurrentFileName(Context context) {
        int vol = getCurrentVol(context);
        String volStr = String.format(Locale.JAPAN, "%02d", vol);
        return FILE_TITLE + getCurrentDateString() + "_vol_" + volStr + FILE_EXT;
    }

    // 現在のvol番号を取得（SharedPreferencesから）
    public static int getCurrentVol(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_VOL, 1);
    }

    // vol番号を+1して保存
    public static void incrementVol(Context context) {
        int currentVol = getCurrentVol(context);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_VOL, currentVol + 1)
                .apply();
    }

    // 現在のファイルオブジェクト取得
    public static File getCurrentFile(Context context) {
        return new File(context.getFilesDir(), getCurrentFileName(context));
    }

    // 登録済みのBIG番号をすべて取得（ファイル内から抽出）
    public static List<Integer> getRegisteredBigNumbers(Context context) {
        List<Integer> result = new ArrayList<>();
        File file = getCurrentFile(context);
        if (!file.exists()) return result;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("【BIG")) {
                    // 例：【BIG　5回目】 → 数字部分だけ取り出す
                    String numStr = line.replaceAll("【BIG　", "")
                            .replaceAll("回目】", "")
                            .trim();
                    try {
                        int num = Integer.parseInt(numStr);
                        result.add(num);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 書き込み処理：BIG回目のキャラ出現をファイルに保存（上書き or 追記）
    public static void overwriteBigDataToFile(Context context, int bigNumber, int[] counts, int totalCount) {
        try {
            File file = getCurrentFile(context);
            List<String> lines = new ArrayList<>();

            // ファイル読み込み（上書きの場合のため）
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }

            // 該当BIGがすでにあるか確認
            int bigIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals("【BIG　" + bigNumber + "回目】")) {
                    bigIndex = i;
                    break;
                }
            }

            // 新しいBIGデータを作成
            List<String> newBlock = new ArrayList<>();
            newBlock.add("【BIG　" + bigNumber + "回目】");
            newBlock.add("");

            CharacterType[] characters = CharacterType.values();
            int mainSum = 0, subSum = 0, succubus = 0, others = 0;

            for (int i = 0; i < characters.length; i++) {
                double rate = totalCount == 0 ? 0 : (double) counts[i] * 100 / totalCount;
                String formatted = String.format(Locale.JAPAN, "%6.2f", rate);
                newBlock.add(String.format(Locale.JAPAN, "%-8s：%2d回 (%s%%)",
                        characters[i].getJapaneseName(), counts[i], formatted));

                switch (characters[i]) {
                    case AQUA:
                    case DARKNESS:
                    case MEGUMIN:
                        mainSum += counts[i];
                        break;

                    case YUNYUN:
                    case WIZ:
                    case CHRIS:
                        subSum += counts[i];
                        break;

                    case SUCCUBUS:
                        succubus += counts[i];
                        break;

                    case OTHERS:
                        others += counts[i];
                        break;
                }

            }

            newBlock.add("");
            newBlock.add(String.format(Locale.JAPAN, "メインキャラ：%2d回 (%6.2f%%)", mainSum, rate(mainSum, totalCount)));
            newBlock.add(String.format(Locale.JAPAN, "サブキャラ　：%2d回 (%6.2f%%)", subSum, rate(subSum, totalCount)));
            newBlock.add(String.format(Locale.JAPAN, "サキュバス　：%2d回 (%6.2f%%)", succubus, rate(succubus, totalCount)));
            newBlock.add(String.format(Locale.JAPAN, "確定系　　　：%2d回 (%6.2f%%)", others, rate(others, totalCount)));
            newBlock.add("");
            newBlock.add("----------------------------------------");
            newBlock.add("");

            // 上書き or 追記処理
            if (bigIndex != -1) {
                int end = bigIndex;
                while (end < lines.size() && !lines.get(end).equals("----------------------------------------")) end++;
                if (end < lines.size()) end++;
                lines.subList(bigIndex, end).clear();
                lines.addAll(bigIndex, newBlock);
            } else {
                if (lines.isEmpty()) {
                    lines.add(FILE_TITLE + getCurrentDateString() + "_vol_" + String.format("%02d", getCurrentVol(context)));
                    lines.add("----------------------------------------");
                }
                lines.addAll(newBlock);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double rate(int count, int total) {
        return total == 0 ? 0.0 : (double) count * 100 / total;
    }
}
