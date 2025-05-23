package com.example.konosuba_chara.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konosuba_chara.adapter.CharacterAdapter;
import com.example.konosuba_chara.enums.CharacterCategory;
import com.example.konosuba_chara.enums.CharacterType;
import com.example.konosuba_chara.model.MiniCharaRecord;
import com.example.konosuba_chara.util.FileUtil;
import com.example.konosuba_chara.R;
import com.example.konosuba_chara.util.MiniCharaRecordUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SettingFragment extends Fragment {

    private CharacterAdapter adapter;
    private TextView bigCountTextView;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Context context = requireContext();

        // BIG回数のTextView取得
        bigCountTextView = view.findViewById(R.id.bigCountTextView);

        // RecyclerView 初期化
        RecyclerView recyclerView = view.findViewById(R.id.characterRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        adapter = new CharacterAdapter(CharacterType.values());
        adapter.loadCountsFromPrefs(context);
        updateAllSummaries( view );  // ← ここを追加
        recyclerView.setAdapter(adapter);


        // カウント変更時の更新処理
        adapter.setOnCountChangedListener(totalCount -> {
            updateAllSummaries( view );
            adapter.saveCountsToPrefs(context);
        });

        // 登録ボタン
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            int totalCount = adapter.getTotalCount();

            // 総出現数が0または6の倍数でない場合は登録不可
            if (totalCount == 0 || totalCount % 6 != 0) {
                Toast.makeText(context, "ミニキャラ出現総数が6の倍数になっていません。登録できません。", Toast.LENGTH_SHORT).show();
                return;
            }

            // 現在のBIG回数を自動計算
            int currentBigNumber = (totalCount + 5) / 6;
            // 常に上書き保存（上書き確認メッセージなしで即実行）
            FileUtil.overwriteBigDataToFile(context, currentBigNumber, adapter.getCounts(), totalCount);


            Toast.makeText(context, currentBigNumber + "回目のデータを保存しました。", Toast.LENGTH_SHORT).show();

            // キャラごとの出現回数
            int[] counts = adapter.getCounts();
            CharacterType[] characters = CharacterType.values();
            Map<CharacterType, Integer> countMap = new EnumMap<>(CharacterType.class);

            for (int i = 0; i < characters.length; i++) {
                countMap.put(characters[i], counts[i]);
            }

            // カテゴリごとの集計（例）
            Map<CharacterCategory, Integer> summaryMap = new EnumMap<>(CharacterCategory.class);

            // カテゴリごとの初期化（0で埋める）
            for (CharacterCategory category : CharacterCategory.values()) {
                summaryMap.put(category, 0);
            }

            // 各キャラの出現数をカテゴリごとに合算
            for (int i = 0; i < characters.length; i++) {
                CharacterType type = characters[i];
                CharacterCategory category = type.getCategory();
                int current = summaryMap.get(category);
                summaryMap.put(category, current + counts[i]);
            }

            Map<CharacterCategory, Double> categoryRateMap = new EnumMap<>(CharacterCategory.class);
            for (CharacterCategory category : CharacterCategory.values()) {
                int count = summaryMap.get(category);
                double rate = totalCount == 0 ? 0.0 : (double) count * 100 / totalCount;
                categoryRateMap.put(category, rate);
            }


            // BIG回数・日時

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN).format(new Date());

//            int[] counts = adapter.getCounts();
//            int totalCount = adapter.getTotalCount();
//            CharacterType[] characters = CharacterType.values();

// 1. BIG回数
            int bigNumber = (totalCount + 5) / 6;

// 2. 日時（現在）
            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN).format(new Date());

// 3. キャラ出現数（名前→回数）
            Map<String, Integer> charaCounts = new LinkedHashMap<>();
            for (int i = 0; i < characters.length; i++) {
                charaCounts.put(characters[i].getJapaneseName(), counts[i]);
            }

// 4. キャラ出現率（名前→割合％）
            Map<String, Double> charaRates = new LinkedHashMap<>();
            for (int i = 0; i < characters.length; i++) {
                double rate = totalCount == 0 ? 0.0 : (double) counts[i] * 100 / totalCount;
                charaRates.put(characters[i].getJapaneseName(), rate);
            }

// 5. カテゴリごとの合計回数
            Map<CharacterCategory, Integer> categoryCountMap = new EnumMap<>(CharacterCategory.class);
            for (CharacterCategory category : CharacterCategory.values()) {
                categoryCountMap.put(category, 0);
            }
            for (int i = 0; i < characters.length; i++) {
                CharacterCategory category = characters[i].getCategory();
                categoryCountMap.put(category, categoryCountMap.get(category) + counts[i]);
            }

// 6. カテゴリごとの出現率（名前→割合％）
            Map<String, Integer> categoryCounts = new LinkedHashMap<>();
            Map<String, Double> categoryRates = new LinkedHashMap<>();
            for (CharacterCategory category : CharacterCategory.values()) {
                int count = categoryCountMap.get(category);
                double rate = totalCount == 0 ? 0.0 : (double) count * 100 / totalCount;
                categoryCounts.put(category.name(), count);
                categoryRates.put(category.name(), rate);
            }

// ✅ インスタンス生成
            MiniCharaRecord record = new MiniCharaRecord(
                    bigNumber,
                    dateTime,
                    charaCounts,
                    charaRates,
                    categoryCounts,
                    categoryRates
            );


            MiniCharaRecordUtil recordFile = new MiniCharaRecordUtil();

            // ✅ JSONとして保存
            boolean jsonSaved = MiniCharaRecordUtil.saveJsonToDownloads(context, record, bigNumber);
            if (!jsonSaved) {
                Toast.makeText(context, "JSON出力に失敗しました", Toast.LENGTH_SHORT).show();
            }



        });

        // リセットボタン
        Button resetButton = view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("確認")
                    .setMessage("データをリセットしますか？")
                    .setPositiveButton("はい", (dialog, which) -> {
                        FileUtil.incrementVol(context);

                        int[] counts = adapter.getCounts();
                        for (int i = 0; i < counts.length; i++) counts[i] = 0;

                        updateAllSummaries(view);
                        adapter.saveCountsToPrefs(context);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(context, "データをリセットしました", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();

//            FileUtil.incrementVol(context);
//
//            int[] counts = adapter.getCounts();
//            for (int i = 0; i < counts.length; i++) counts[i] = 0;
//
//            updateAllSummaries( view );
//            adapter.saveCountsToPrefs(context);
//            adapter.notifyDataSetChanged();
//
//            Toast.makeText(context, "データをリセットしました", Toast.LENGTH_SHORT).show();
        });

        // TODO: 全件表示・合計表示ボタンの処理も後で追加可能

        // ファイル出力ボタン
        Button exportButton = view.findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            int[] counts = adapter.getCounts();
            int totalCount = adapter.getTotalCount();

            if (totalCount == 0 || totalCount % 6 != 0) {
                Toast.makeText(context, "6の倍数になっていません。出力できません。", Toast.LENGTH_SHORT).show();
                return;
            }


            //
            new AlertDialog.Builder(context)
                    .setTitle("ファイル出力の確認")
                    .setMessage("Downloadsフォルダに出力し、データをリセットしますか？")
                    .setPositiveButton("はい", (dialog, which) -> {
//                        boolean result = FileUtil.exportMiniCharaDataToDownloads(context, counts, totalCount);
                        boolean result = FileUtil.exportMiniCharaDataToDownloads(context,  counts, totalCount);

                        if (result) {
                            // 🔽 出力成功時に vol を進める
                            FileUtil.incrementVol(context);
                            // カウントをクリア
                            for (int i = 0; i < counts.length; i++) counts[i] = 0;
                            adapter.saveCountsToPrefs(context);
                            adapter.notifyDataSetChanged();
                            updateAllSummaries(view);
                            Toast.makeText(context, "Downloads に出力し、データをリセットしました。", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "出力に失敗しました。", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();


            // ファイル出力（Download フォルダへ保存）
//            boolean result = FileUtil.exportMiniCharaDataToDownloads(context, counts, totalCount);
//            if (result) {
//                // 出力成功 → 入力リセット
//                for (int i = 0; i < counts.length; i++) counts[i] = 0;
//                adapter.saveCountsToPrefs(context);
//                adapter.notifyDataSetChanged();
//                updateAllSummaries( view );
//                Toast.makeText(context, "Downloads に出力し、データをリセットしました。", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "出力に失敗しました。", Toast.LENGTH_SHORT).show();
//            }
        });




        return view;
    }

    private void updateAllSummaries( View view ) {
        int[] counts = adapter.getCounts();
        int totalCount = adapter.getTotalCount();

        int mainSum = 0, subSum = 0, succubus = 0, others = 0;
        for (int i = 0; i < counts.length; i++) {

            switch (CharacterType.values()[i]) {
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


//        updateSummary(R.id.mainCount, R.id.mainRate, mainSum, totalCount);
//        updateSummary(R.id.subCount, R.id.subRate, subSum, totalCount);
//        updateSummary(R.id.succubusCount, R.id.succubusRate, succubus, totalCount);
//        updateSummary(R.id.othersCount, R.id.othersRate, others, totalCount);

        updateSummary(view, R.id.mainCount, R.id.mainRate, mainSum, totalCount);
        updateSummary(view, R.id.subCount, R.id.subRate, subSum, totalCount);
        updateSummary(view, R.id.succubusCount, R.id.succubusRate, succubus, totalCount);
        updateSummary(view, R.id.othersCount, R.id.othersRate, others, totalCount);


        int bigCount = (totalCount + 5) / 6;
        bigCountTextView.setText("BIG回数：" + bigCount + "回　（総出現数：" + totalCount + "）");
    }

    private void updateSummary(View rootView, int countViewId, int rateViewId, int count, int total) {
//        TextView countView = requireView().findViewById(countViewId);
//        TextView rateView = requireView().findViewById(rateViewId);
        TextView countView = rootView.findViewById(countViewId);
        TextView rateView = rootView.findViewById(rateViewId);

        countView.setText(count + "回");

        double rate = total == 0 ? 0 : (double) count * 100 / total;
        String formatted = String.format(Locale.JAPAN, "%.2f%%", rate);
        rateView.setText(formatted);
    }
}
