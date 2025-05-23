package com.example.konosuba_chara.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.konosuba_chara.data.BonusEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BonusFileUtil {
    // ファイル名のタイトル部分
    private static final String FILE_TITLE = "【A+このすばボーナス詳細】";
    // ファイルの拡張子
    private static final String FILE_EXT = ".txt";
    // SharedPreferences の保存名とキー
    private static final String PREFS_NAME = "bonus_file_prefs";
    private static final String KEY_VOL = "bonus_current_vol";

    // 📅 現在の日付を yyyy_MM_dd 形式で取得（ファイル名生成用）
    public static String getCurrentDateString() {
        return new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
    }

    // 🔢 SharedPreferences から現在の vol 番号を取得
    public static int getCurrentVol(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_VOL, 1);
    }

    // ➕ vol 番号を +1 して保存（リセットや新規作成時に使用）
    public static void incrementVol(Context context) {
        int currentVol = getCurrentVol(context);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_VOL, currentVol + 1)
                .apply();
    }

    // 📄 現在のファイル名を生成（例：【A+このすばボーナス詳細】2025_05_07_vol_01.txt）
    public static String getCurrentFileName(Context context) {
        int vol = getCurrentVol(context);
        String volStr = String.format(Locale.JAPAN, "%02d", vol);
        return FILE_TITLE + getCurrentDateString() + "_vol_" + volStr + FILE_EXT;
    }

    // 📁 アプリ内ファイルディレクトリから現在の File オブジェクトを取得
    public static File getCurrentFile(Context context) {
        return new File(context.getFilesDir(), getCurrentFileName(context));
    }

    // 📝 「開始」ボタン押下時に呼ばれる：ファイル作成＆ヘッダー行を書き込み
    public static void createBonusFileWithHeader(Context context, Integer startGame) {
        File file = getCurrentFile(context);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            // タイトル行
            writer.write(getCurrentFileName(context).replace(FILE_EXT, ""));
            writer.newLine();

            // 開始ゲーム数が0以外なら記述
            if (startGame != null && startGame > 0) {
                writer.write("(開始ゲーム数：" + startGame + ")");
                writer.newLine();
            }

            writer.newLine(); // 空行
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 永久欠番用：最終使用済みvol番号を取得
    public static int getLastUsedVol(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt("last_used_vol", 0);
    }

    // 永久欠番用：新しいvol番号を1つ進めて保存し、返す
    public static int incrementAndGetVol(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int newVol = getLastUsedVol(context) + 1;
        prefs.edit().putInt("last_used_vol", newVol).apply();
        return newVol;
    }

    // 指定volでファイル名取得
    public static String getFileNameForVol(int vol) {
        String date = getCurrentDateString();
        String volStr = String.format(Locale.JAPAN, "%02d", vol);
        return FILE_TITLE + date + "_vol_" + volStr + FILE_EXT;
    }

    // 指定volでファイルオブジェクト取得
    public static File getFileForVol(Context context, int vol) {
        return new File(context.getFilesDir(), getFileNameForVol(vol));
    }

    public static void writeBonusEntryToFile(Context context, BonusEntry entry) {
        File file = getCurrentFile(context);

        String line = String.format(Locale.JAPAN, "%d\t%s\t%s\t%s\t%s",
                entry.getGame(),
                entry.getTrigger(),
                entry.getTriggerCount(),
                entry.getBonusType(),
                entry.getNote());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
