package com.example.konosuba_chara.util;


import static com.example.konosuba_chara.util.FileUtil.getCurrentJsonFileName;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MiniCharaRecordUtil {
    private static final String FILE_TITLE = "【A+このすばBIG中ぬいぐるみ累計】";
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


    public static String getCategoryDisplayName(CharacterCategory category) {
        switch (category) {
            case MAIN:
                return "メインキャラ";
            case SUB:
                return "サブキャラ";
            case SUCCUBUS:
                return "サキュバス";
            case FIXED:
                return "確定系";
            default:
                return category.name(); // 念のため
        }
    }
    public static Map<String, Integer> buildCategoryCountsWithJapaneseKeys(int[] counts) {
        Map<String, Integer> result = new LinkedHashMap<>();
        CharacterType[] characters = CharacterType.values();

        // カタカナのキーで初期化
        for (CharacterCategory category : CharacterCategory.values()) {
            result.put(getCategoryDisplayName(category), 0);
        }

        // 集計
        for (int i = 0; i < characters.length; i++) {
            CharacterCategory category = characters[i].getCategory();
            String key = getCategoryDisplayName(category);
            result.put(key, result.get(key) + counts[i]);
        }

        return result;
    }
    public static Map<String, Double> buildCategoryRatesWithJapaneseKeys(Map<String, Integer> categoryCounts, int totalCount) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            double rate = totalCount == 0 ? 0.0 : (double) entry.getValue() * 100 / totalCount;
            result.put(entry.getKey(), rate);
        }
        return result;
    }



    public static boolean exportJsonRecord(Context context, int[] counts, int bigNumber, int totalCount) {
        // 1. キャラ出現数・率
        Map<String, Integer> charaCounts = buildCharaCounts(counts);
        Map<String, Double> charaRates = buildCharaRates(counts, totalCount);

        // 2. カテゴリ集計
//        Map<String, Integer> categoryCounts = buildCategoryCounts(counts);
//        Map<String, Double> categoryRates = buildCategoryRates(categoryCounts, totalCount);
        // 2. カテゴリ（カタカナで集計）
        Map<String, Integer> categoryCounts = buildCategoryCountsWithJapaneseKeys(counts);
        Map<String, Double> categoryRates = buildCategoryRatesWithJapaneseKeys(categoryCounts, totalCount);

        // 3. 日付 & ファイル名
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN).format(new Date());
        String fileName = generateFileName(context, bigNumber);

        // 4. レコード生成 & 出力
        MiniCharaRecord record = new MiniCharaRecord(fileName, bigNumber, dateTime, charaCounts, charaRates, categoryCounts, categoryRates);
        return saveJsonToDownloads(context, record, bigNumber);
    }


    public static List<Uri> findAllCumulativeJsonFiles(Context context) {
        List<Uri> matchingFiles = new ArrayList<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return matchingFiles;
        }

        ContentResolver resolver = context.getContentResolver();
        String volumePart = FileUtil.getCurrentJsonFileName(context); // 例：_vol_12

        // 累計ファイル名プレフィックス（前方一致）
        String filePrefix = "【A+このすばBIG中ぬいぐるみ累計】";

        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL);
        String[] projection = {
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME
        };

        String selection = MediaStore.Downloads.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = new String[] { filePrefix + "%" + volumePart + "%" };

        try (Cursor cursor = resolver.query(collection, projection, selection, selectionArgs, null)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri fileUri = ContentUris.withAppendedId(collection, id);
                    matchingFiles.add(fileUri);
                }
            }
        }

        return matchingFiles;
    }

    public static List<MiniCharaRecord> readCumulativeJsonRecords(Context context) {
        List<MiniCharaRecord> records = new ArrayList<>();
        List<Uri> fileUris = findAllCumulativeJsonFiles(context);

        Gson gson = new Gson();

        for (Uri uri : fileUris) {
            try (InputStream is = context.getContentResolver().openInputStream(uri);
                 InputStreamReader reader = new InputStreamReader(is)) {

                MiniCharaRecord record = gson.fromJson(reader, MiniCharaRecord.class);
                if (record != null) {
                    records.add(record);
                }

            } catch (Exception e) {
                e.printStackTrace();
                // 読み取りに失敗しても処理継続（不正なファイルはスキップ）
            }
        }

        return records;
    }

    public static String formatRecordList(List<MiniCharaRecord> records) {
        StringBuilder builder = new StringBuilder();

        // BIG回数でソート（昇順）
        records.sort(Comparator.comparingInt(MiniCharaRecord::getBigNumber));

        for (MiniCharaRecord record : records) {
            builder.append("【BIG ").append(record.getBigNumber()).append("回目】\n");

            Map<String, Integer> charaCounts = record.getCharaCounts();
            Map<String, Double> charaRates = record.getCharaRates();

            for (String name : charaCounts.keySet()) {
                int count = charaCounts.get(name);
                double rate = charaRates.getOrDefault(name, 0.0);
                builder.append(String.format(Locale.JAPAN, "%-8s： %2d回 (%6.2f%%)\n", name, count, rate));
            }

            builder.append("\n");

            Map<String, Integer> categoryCounts = record.getCategoryCounts();
            Map<String, Double> categoryRates = record.getCategoryRates();

            for (String category : categoryCounts.keySet()) {
                int count = categoryCounts.get(category);
                double rate = categoryRates.getOrDefault(category, 0.0);
                builder.append(String.format(Locale.JAPAN, "%-8s： %2d回 (%6.2f%%)\n", category, count, rate));
            }

            builder.append("\n");
        }

        return builder.toString();
    }


//    public static String buildBigDiffText(List<MiniCharaRecord> records) {
//        if (records == null || records.isEmpty()) return "データがありません";
//
//        StringBuilder builder = new StringBuilder();
//
//        // BIG回数順にソート
//        List<MiniCharaRecord> sorted = new ArrayList<>(records);
//        sorted.sort(Comparator.comparingInt(MiniCharaRecord::getBigNumber));
//
//        Map<String, Integer> previousCounts = new LinkedHashMap<>();
//
//        for (MiniCharaRecord current : sorted) {
//            Map<String, Integer> currentCounts = current.getCharaCounts();
//            Map<String, Integer> diffCounts = new LinkedHashMap<>();
//            int diffTotal = 0;
//
//            // 差分計算
//            for (Map.Entry<String, Integer> entry : currentCounts.entrySet()) {
//                String name = entry.getKey();
//                int currentValue = entry.getValue();
//                int previousValue = previousCounts.getOrDefault(name, 0);
//                int diff = currentValue - previousValue;
//                diffCounts.put(name, diff);
//                diffTotal += diff;
//            }
//
//            // 見出し
//            builder.append("【BIG ").append(current.getBigNumber()).append("回目】\n");
//
//            // 各キャラの出現数と出現率を出力
//            for (Map.Entry<String, Integer> entry : diffCounts.entrySet()) {
//                String name = entry.getKey();
//                int count = entry.getValue();
//                double rate = diffTotal == 0 ? 0.0 : (double) count * 100 / diffTotal;
//                builder.append(String.format(Locale.JAPAN, "%-6s：%2d回 (%6.2f%%)\n", name, count, rate));
//            }
//
//            builder.append("\n");
//
//            // 今回のデータを次回比較用に保存
//            previousCounts = new LinkedHashMap<>(currentCounts);
//        }
//
//        return builder.toString();
//    }

    public static String buildBigDiffText(List<MiniCharaRecord> records) {
        if (records == null || records.isEmpty()) return "データがありません";

        StringBuilder builder = new StringBuilder();

        // BIG回数順にソート
        List<MiniCharaRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparingInt(MiniCharaRecord::getBigNumber));

        Map<String, Integer> previousCharaCounts = new LinkedHashMap<>();
        Map<String, Integer> previousCategoryCounts = new LinkedHashMap<>();

        for (MiniCharaRecord current : sorted) {
            Map<String, Integer> currentCharaCounts = current.getCharaCounts();
            Map<String, Integer> diffCharaCounts = new LinkedHashMap<>();
            int totalCharaDiff = 0;

            // キャラごとの差分計算
            for (Map.Entry<String, Integer> entry : currentCharaCounts.entrySet()) {
                String name = entry.getKey();
                int currentValue = entry.getValue();
                int prevValue = previousCharaCounts.getOrDefault(name, 0);
                int diff = currentValue - prevValue;
                diffCharaCounts.put(name, diff);
                totalCharaDiff += diff;
            }

            // タイトル
            builder.append("【BIG ").append(current.getBigNumber()).append("回目】\n");

            // キャラ出現差分表示
            for (Map.Entry<String, Integer> entry : diffCharaCounts.entrySet()) {
                String name = entry.getKey();
                int count = entry.getValue();
                double rate = totalCharaDiff == 0 ? 0.0 : (double) count * 100 / totalCharaDiff;
                builder.append(String.format(Locale.JAPAN, "%-6s：%2d回 (%6.2f%%)\n", name, count, rate));
            }

            builder.append("\n");

            // カテゴリごとの差分も表示
            Map<String, Integer> currentCategoryCounts = current.getCategoryCounts();
            Map<String, Integer> diffCategoryCounts = new LinkedHashMap<>();
            int totalCategoryDiff = 0;

            for (Map.Entry<String, Integer> entry : currentCategoryCounts.entrySet()) {
                String name = entry.getKey();
                int currentValue = entry.getValue();
                int prevValue = previousCategoryCounts.getOrDefault(name, 0);
                int diff = currentValue - prevValue;
                diffCategoryCounts.put(name, diff);
                totalCategoryDiff += diff;
            }

            // カテゴリ出現差分表示
            for (Map.Entry<String, Integer> entry : diffCategoryCounts.entrySet()) {
                String name = entry.getKey();
                int count = entry.getValue();
                double rate = totalCategoryDiff == 0 ? 0.0 : (double) count * 100 / totalCategoryDiff;
                builder.append(String.format(Locale.JAPAN, "%-6s：%2d回 (%6.2f%%)\n", name, count, rate));
            }

            builder.append("\n");

            // 次回比較用に保存
            previousCharaCounts = new LinkedHashMap<>(currentCharaCounts);
            previousCategoryCounts = new LinkedHashMap<>(currentCategoryCounts);
        }

        return builder.toString();
    }



}
