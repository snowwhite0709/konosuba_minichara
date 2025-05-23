package com.example.konosuba_chara.util;


import static com.example.konosuba_chara.util.FileUtil.getCurrentJsonFileName;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.net.Uri;
import android.widget.Toast;

import com.example.konosuba_chara.enums.CharacterCategory;
import com.example.konosuba_chara.enums.CharacterType;
import com.example.konosuba_chara.model.MiniCharaRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MiniCharaRecordUtil {
    private static final String FILE_TITLE = "【A+このすばBIG中ぬいぐるみ】";
    private static final String FILE_EXT = ".json";



    /**
     * 『JSONファイル名』文字列生成メソッド
     */
    public static String generateFileName(Context context, int bigNumber) {
        String timestamp = new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
        return FILE_TITLE + timestamp + FileUtil.getCurrentJsonFileName(context) + "_" + bigNumber + "回目" + FILE_EXT;
    }


    /**
     * MiniCharaRecord オブジェクトを JSON ファイルとして Downloads フォルダに保存する。
     *
     * @param context    コンテキスト（Activity や Fragment など）
     * @param record     保存するミニキャラ出現データ（1BIG分）
     * @param bigNumber  保存対象のBIG回数（例：3）
     * @return 保存が成功したかどうか（true: 成功 / false: 失敗）
     */
    public static boolean saveJsonToDownloads(Context context, MiniCharaRecord record, int bigNumber) {
        // Android 10未満ではScoped Storage非対応のため保存不可
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(context, "Android 10以上で対応しています", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ 保存ファイル名を作成
        // String timestamp = new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
        String fileName = generateFileName(context, bigNumber);

        // ✅ MediaStore に登録するファイル情報（ファイル名、MIMEタイプ、保存先）
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName); // ファイル名
        values.put(MediaStore.Downloads.MIME_TYPE, "application/json"); // JSONとして扱う
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS); // 保存先：Downloadsフォルダ

        // ✅ MediaStore経由で保存用のURIを取得
        Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(context, "保存に失敗しました（URI取得不可）", Toast.LENGTH_SHORT).show();
            return false;
        }

        try (// ✅ OutputStream を取得して BufferedWriter にラップ
                OutputStream out = context.getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))
        ) {
            // ✅ Gson を使ってJSON文字列を生成（整形付き）
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(record); // MiniCharaRecord → JSON文字列
            // ✅ JSONをファイルに書き込み
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



        public static Map<String, Integer> buildCharaCounts(int[] counts) {
            CharacterType[] characters = CharacterType.values();
            Map<String, Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < characters.length; i++) {
                map.put(characters[i].getJapaneseName(), counts[i]);
            }
            return map;
        }

        public static Map<String, Double> buildCharaRates(int[] counts, int totalCount) {
            CharacterType[] characters = CharacterType.values();
            Map<String, Double> map = new LinkedHashMap<>();
            for (int i = 0; i < characters.length; i++) {
                double rate = totalCount == 0 ? 0.0 : (double) counts[i] * 100 / totalCount;
                map.put(characters[i].getJapaneseName(), rate);
            }
            return map;
        }

        public static Map<String, Integer> buildCategoryCounts(int[] counts) {
            CharacterType[] characters = CharacterType.values();
            Map<CharacterCategory, Integer> temp = new EnumMap<>(CharacterCategory.class);
            for (CharacterCategory category : CharacterCategory.values()) {
                temp.put(category, 0);
            }
            for (int i = 0; i < characters.length; i++) {
                CharacterCategory category = characters[i].getCategory();
                temp.put(category, temp.get(category) + counts[i]);
            }
            Map<String, Integer> result = new LinkedHashMap<>();
            for (CharacterCategory category : CharacterCategory.values()) {
                result.put(category.name(), temp.get(category));
            }
            return result;
        }

        public static Map<String, Double> buildCategoryRates(Map<String, Integer> categoryCounts, int totalCount) {
            Map<String, Double> map = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                double rate = totalCount == 0 ? 0.0 : (double) entry.getValue() * 100 / totalCount;
                map.put(entry.getKey(), rate);
            }
            return map;
        }

    public static boolean exportJsonRecord(Context context, int[] counts, int bigNumber, int totalCount) {
        // 1. キャラ出現数・率
        Map<String, Integer> charaCounts = buildCharaCounts(counts);
        Map<String, Double> charaRates = buildCharaRates(counts, totalCount);

        // 2. カテゴリ集計
        Map<String, Integer> categoryCounts = buildCategoryCounts(counts);
        Map<String, Double> categoryRates = buildCategoryRates(categoryCounts, totalCount);

        // 3. 日付 & ファイル名
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN).format(new Date());
        String fileName = generateFileName(context, bigNumber);

        // 4. レコード生成 & 出力
        MiniCharaRecord record = new MiniCharaRecord(fileName, bigNumber, dateTime, charaCounts, charaRates, categoryCounts, categoryRates);
        return saveJsonToDownloads(context, record, bigNumber);
    }





}
