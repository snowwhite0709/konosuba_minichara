package com.example.konosuba_chara.util;


import static com.example.konosuba_chara.util.FileUtil.getCurrentJsonFileName;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.net.Uri;
import android.widget.Toast;

import com.example.konosuba_chara.model.MiniCharaRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MiniCharaRecordUtil {
    private static final String FILE_TITLE = "【A+このすばBIG中ぬいぐるみ】";
    private static final String FILE_EXT = ".json";
    private static final String PREFS_NAME = "file_prefs";
    private static final String KEY_VOL = "current_vol";




    /**
     * JSONファイルとしてDownloadsに保存
     */
    public static boolean saveJsonToDownloads(Context context, MiniCharaRecord record, int bigNumber) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(context, "Android 10以上で対応しています", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ファイル名：ミニキャラ_yyyy_MM_dd_HH_mm_ss.json
//        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.JAPAN).format(new Date());
        String timestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.JAPAN).format(new Date());
        String fileName = FILE_TITLE + timestamp + "_vol_" + FileUtil.getCurrentJsonFileName(context) + "_" + bigNumber + "回目"  + FILE_EXT;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(context, "保存に失敗しました（URI取得不可）", Toast.LENGTH_SHORT).show();
            return false;
        }

        try (OutputStream out = context.getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(record);

            writer.write(json);
            writer.flush();

            Toast.makeText(context, "JSONファイルを保存しました", Toast.LENGTH_SHORT).show();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
