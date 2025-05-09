package com.example.konosuba_chara;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BonusFileUtil {

    private static final String FILE_TITLE = "【A+このすばボーナス詳細】";
    private static final String FILE_EXT = ".txt";
    private static final String PREFS_NAME = "bonus_file_prefs";
    private static final String KEY_VOL = "bonus_current_vol";

    // 現在の日付を取得（例：2025_05_07）
    public static String getCurrentDateString() {
        return new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
    }

    // 現在のvol番号を取得
    public static int getCurrentVol(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_VOL, 1);
    }

    // vol番号を+1して保存
    public static void incrementVol(Context context) {
        int currentVol = getCurrentVol(context);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_VOL, currentVol + 1)
                .apply();
    }

    // 現在のファイル名を取得（例：【A+このすばボーナス詳細】2025_05_07_vol_01.txt）
    public static String getCurrentFileName(Context context) {
        int vol = getCurrentVol(context);
        String volStr = String.format(Locale.JAPAN, "%02d", vol);
        return FILE_TITLE + getCurrentDateString() + "_vol_" + volStr + FILE_EXT;
    }

    // 現在のファイルオブジェクトを取得
    public static File getCurrentFile(Context context) {
        return new File(context.getFilesDir(), getCurrentFileName(context));
    }

    // 「開始」時にファイル作成＆ヘッダー書き込み
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
}
