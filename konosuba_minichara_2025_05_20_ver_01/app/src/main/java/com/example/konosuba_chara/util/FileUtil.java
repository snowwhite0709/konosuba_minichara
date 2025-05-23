package com.example.konosuba_chara.util;


import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.konosuba_chara.enums.CharacterType;

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


    /*
    public static boolean exportMiniCharaDataToDownloads(Context context, int[] counts, int totalCount) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(context, "この機能はAndroid 10以上で対応しています", Toast.LENGTH_SHORT).show();
            return false;
        }

        String dateStr = new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
        String fileName = "ミニキャラ出現データ_" + dateStr + ".txt";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);




        if (uri == null) {
            Toast.makeText(context, "保存先のURIが取得できませんでした", Toast.LENGTH_SHORT).show();
            return false;
        }

        try (OutputStream out = context.getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {

            writer.write("【ミニキャラ出現データ】");
            writer.newLine();
            writer.write("BIG回数：" + ((totalCount + 5) / 6) + "回");
            writer.newLine();
            writer.newLine();

            CharacterType[] characters = CharacterType.values();
            for (int i = 0; i < characters.length; i++) {
                writer.write(characters[i].getJapaneseName() + "\t" + counts[i] + "回");
                writer.newLine();
            }

            writer.newLine();
            writer.write("合計：" + totalCount + "回");
            writer.newLine();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


     */

    public static boolean exportMiniCharaDataToDownloads(Context context, int[] counts, int totalCount) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(context, "この機能はAndroid 10以上で対応しています", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 🔸 登録済みファイルのパスを取得
        File sourceFile = getCurrentFile(context);
        if (!sourceFile.exists()) {
            Toast.makeText(context, "出力対象のファイルが見つかりません", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 🔸 ファイル名そのまま使用
        String fileName = sourceFile.getName();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(context, "保存先のURIが取得できませんでした", Toast.LENGTH_SHORT).show();
            return false;
        }

        try (
                InputStream in = new FileInputStream(sourceFile);
                OutputStream out = context.getContentResolver().openOutputStream(uri);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }


            writer.write("【ミニキャラ出現データ】");
            writer.newLine();
            writer.write("BIG回数：" + ((totalCount + 5) / 6) + "回");
            writer.newLine();
            writer.newLine();

            CharacterType[] characters = CharacterType.values();
            for (int i = 0; i < characters.length; i++) {
                writer.write(characters[i].getJapaneseName() + "\t" + counts[i] + "回");
                writer.newLine();
            }

            writer.newLine();
            writer.write("合計：" + totalCount + "回");
            writer.newLine();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


}
