package com.example.konosuba_chara.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingFragment extends Fragment {

    private CharacterAdapter adapter;
    private TextView bigCountTextView;

    Button allDisplayButton;
    Button summaryDisplayButton;
    private boolean isAllDisplayVisible = false;  // 全件表示の表示状態
    private boolean isSummaryDisplayVisible = false; // 合計表示の表示状態


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
            // ✅ 共通メソッドでバリデーション（6の倍数か？）
            if (!validateOutput(context, totalCount)) return;
            // 現在のBIG回数を自動計算
            int currentBigNumber = (totalCount + 5) / 6;

            // --- 🔽 ファイル出力処理 ---
            // 常に上書き保存（ // ファイル出力（txt形式）
            FileUtil.overwriteBigDataToFile(context, currentBigNumber, adapter.getCounts(), totalCount);
            Toast.makeText(context, currentBigNumber + "回目のデータを保存しました。", Toast.LENGTH_SHORT).show();

            // --- 🔽 JSON出力処理 ---
            // キャラごとの出現回数
            int[] counts = adapter.getCounts();

            boolean result = MiniCharaRecordUtil.exportJsonRecord(context, counts, currentBigNumber, totalCount);
            if (!result) {
                Toast.makeText(context, "JSON出力に失敗しました", Toast.LENGTH_SHORT).show();
            }

        });

        // リセットボタン
        Button resetButton = view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            // 🔽 ダイアログを表示して確認
            showConfirmationDialog(context, "確認", "データをリセットしますか？", () -> {
                // 🔁 リセット前の値を保存（Undo用）
                int[] backupCounts = Arrays.copyOf(adapter.getCounts(), adapter.getCounts().length);
                // 🔽 vol番号（ファイル通番）を1つ進める（次のファイル用）
                FileUtil.incrementVol(context);
                // ✅ 共通メソッドでリセット処理
                resetCountsAndRefreshUI(view);
                // 🔽 表示領域を閉じる
                view.findViewById(R.id.outputScrollView).setVisibility(View.GONE);
                // 元に戻す処理（Undo）
                Snackbar.make(view, "データをリセットしました", Snackbar.LENGTH_LONG)
                        .setAction("元に戻す", undoView -> {
                            System.arraycopy(backupCounts, 0, adapter.getCounts(), 0, backupCounts.length);
                            adapter.saveCountsToPrefs(context);
                            adapter.notifyDataSetChanged();
                            updateAllSummaries(view);
                            Toast.makeText(context, "リセットを取り消しました", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            });

        });

        // 各BIG累計確率 全件表示ボタン
        // 🔽 必ず view.findViewById(...) を使って取得
        TextView outputTextView = view.findViewById(R.id.outputTextView);
        ScrollView outputScrollView = view.findViewById(R.id.outputScrollView);
        allDisplayButton = view.findViewById(R.id.allDisplayButton);


        // 全件表示ボタン
        allDisplayButton.setOnClickListener(v -> {
            List<MiniCharaRecord> records = MiniCharaRecordUtil.readCumulativeJsonRecords(context);
            if (records.isEmpty()) {
                Toast.makeText(context, "該当するデータがありません", Toast.LENGTH_SHORT).show();
                return;
            }

            // すでに全件表示中なら → 非表示にする
            if (isAllDisplayVisible) {
                outputScrollView.setVisibility(View.GONE);
                allDisplayButton.setBackgroundColor(ContextCompat.getColor(context, R.color.default_button));
                isAllDisplayVisible = false;
                return;
            }

            // ✅ ここから全件表示モードに切り替える
            String outputText = MiniCharaRecordUtil.buildBigDiffText(records);

// ✅ 表示内容の先頭にタイトルを追加
            StringBuilder fullText = new StringBuilder();
            fullText.append("【各BIG累計確率全件表示】\n\n"); // ← 表示タイトル
            fullText.append(outputText);

// 表示
            outputTextView.setText(fullText.toString());
            outputScrollView.setVisibility(View.VISIBLE);

            // ボタン色切り替え
            allDisplayButton.setBackgroundColor(ContextCompat.getColor(context, R.color.highlight_all));
            summaryDisplayButton.setBackgroundColor(ContextCompat.getColor(context, R.color.default_button));

            // フラグ更新
            isAllDisplayVisible = true;
            isSummaryDisplayVisible = false;
        });




        // 各BIG累計確率 合計のみ表示ボタン
        summaryDisplayButton = view.findViewById(R.id.summaryDisplayButton);

        // 合計のみ表示ボタン
        summaryDisplayButton.setOnClickListener(v -> {
            List<MiniCharaRecord> records = MiniCharaRecordUtil.readCumulativeJsonRecords(context);
            if (records.isEmpty()) {
                Toast.makeText(context, "該当するデータがありません", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSummaryDisplayVisible) {
                outputScrollView.setVisibility(View.GONE);
                summaryDisplayButton.setBackgroundColor(ContextCompat.getColor(context, R.color.default_button));
                isSummaryDisplayVisible = false;
                return;
            }

            // ✅ 合計表示に切り替える
            String output = MiniCharaRecordUtil.formatRecordList(records);

            StringBuilder fullText = new StringBuilder();
            fullText.append("【各BIG累計確率表示（合計）】\n\n"); // ← 表示タイトル
            fullText.append(output);

            outputTextView.setText(fullText.toString());
            outputScrollView.setVisibility(View.VISIBLE);

            // ボタン色切り替え
            summaryDisplayButton.setBackgroundColor(ContextCompat.getColor(context, R.color.highlight_all));
            allDisplayButton.setBackgroundColor(ContextCompat.getColor(context, R.color.default_button));

            // フラグ更新
            isSummaryDisplayVisible = true;
            isAllDisplayVisible = false;
        });



        // ファイル出力ボタン（Downloads フォルダに保存 + 入力内容をリセット）
        Button exportButton = view.findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            // 現在のカウント情報を取得
            int[] counts = adapter.getCounts();
            int totalCount = adapter.getTotalCount();
            // 出現総数が0 または 6の倍数でない場合は出力不可
            if (!validateOutput(context, totalCount)) return;

            // 確認ダイアログ表示（出力＋リセットの確認）
            showConfirmationDialog(context, "ファイル出力の確認", "Downloadsフォルダに出力し、データをリセットしますか？", () -> {
                // ファイル出力処理（保存できれば true）
                boolean result = FileUtil.exportMiniCharaDataToDownloads(context, counts, totalCount);
                if (result) {
                    // ✅ 出力成功時：vol番号を進める（ファイル名用）
                    FileUtil.incrementVol(context);
                    // ✅ 共通メソッドでリセット処理
                    resetCountsAndRefreshUI(view);
                    // 🔽 表示領域を閉じる
                    view.findViewById(R.id.outputScrollView).setVisibility(View.GONE);

                    Toast.makeText(context, "Downloads に出力し、データをリセットしました。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "出力に失敗しました。", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void resetCountsAndRefreshUI(View view) {
        int[] counts = adapter.getCounts();
        Arrays.fill(counts, 0); // 全て0にする
        adapter.saveCountsToPrefs(requireContext()); // SharedPreferencesに保存
        adapter.notifyDataSetChanged(); // RecyclerViewを更新
        updateAllSummaries(view); // サマリーを再計算して表示
        // 表示領域を非表示にする
        ScrollView scrollView = view.findViewById(R.id.outputScrollView);
        scrollView.setVisibility(View.GONE);
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


        updateSummary(view, R.id.mainCount, R.id.mainRate, mainSum, totalCount);
        updateSummary(view, R.id.subCount, R.id.subRate, subSum, totalCount);
        updateSummary(view, R.id.succubusCount, R.id.succubusRate, succubus, totalCount);
        updateSummary(view, R.id.othersCount, R.id.othersRate, others, totalCount);


        int bigCount = (totalCount + 5) / 6;
        bigCountTextView.setText("BIG回数：" + bigCount + "回　（総出現数：" + totalCount + "）");
    }

    private void updateSummary(View rootView, int countViewId, int rateViewId, int count, int total) {
        TextView countView = rootView.findViewById(countViewId);
        TextView rateView = rootView.findViewById(rateViewId);

        countView.setText(count + "回");

        double rate = total == 0 ? 0 : (double) count * 100 / total;
        String formatted = String.format(Locale.JAPAN, "%.2f%%", rate);
        rateView.setText(formatted);
    }

    /**
     * 確認用の共通ダイアログを表示するヘルパーメソッド
     *
     * @param context Context（通常は requireContext() でOK）
     * @param title ダイアログのタイトル
     * @param message ダイアログに表示するメッセージ
     * @param positiveAction 「はい」選択時の処理（Runnable）
     */
    private void showConfirmationDialog(Context context, String title, String message, Runnable positiveAction) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("はい", (dialog, which) -> positiveAction.run())
                .setNegativeButton("キャンセル", null)
                .show();
    }


    /**
     * 出力前に6の倍数であるかチェックし、メッセージ表示付きでバリデーション
     * @param context コンテキスト
     * @param totalCount 総出現回数
     * @return trueなら出力可能、falseならエラーメッセージ表示後に出力中止
     */
    private boolean validateOutput(Context context, int totalCount) {
        if (totalCount == 0 || totalCount % 6 != 0) {
            Toast.makeText(context, "6の倍数になっていません。出力できません。", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    private void toggleAllDisplay(View view, Button allDisplayButton, ScrollView outputScrollView, TextView outputTextView, Context context) {
        if (isAllDisplayVisible) {
            // 非表示にする
            outputScrollView.setVisibility(View.GONE);
            outputTextView.setText("");
            allDisplayButton.setBackgroundColor(getResources().getColor(R.color.default_button));
            isAllDisplayVisible = false;
        } else {
            // 表示する処理（例：差分の全件テキストを生成）
            String displayText = MiniCharaRecordUtil. buildBigDiffText( MiniCharaRecordUtil.readCumulativeJsonRecords(context)) ;
            outputTextView.setText(displayText);
            outputScrollView.setVisibility(View.VISIBLE);

            // 色を変える（他のボタンをデフォルトに戻す）
            allDisplayButton.setBackgroundColor(getResources().getColor(R.color.highlight_all));
            summaryDisplayButton.setBackgroundColor(getResources().getColor(R.color.default_button));

            isAllDisplayVisible = true;
            isSummaryDisplayVisible = false;
        }
    }

    private void toggleSummaryDisplay(View view, Button summaryDisplayButton, ScrollView outputScrollView, TextView outputTextView, Context context) {
        if (isSummaryDisplayVisible) {
            // 非表示にする
            outputScrollView.setVisibility(View.GONE);
            outputTextView.setText("");
            summaryDisplayButton.setBackgroundColor(getResources().getColor(R.color.default_button));
            isSummaryDisplayVisible = false;
        } else {
            String summaryText = MiniCharaRecordUtil.formatRecordList(
                    MiniCharaRecordUtil.readCumulativeJsonRecords(context)
            );
            outputTextView.setText(summaryText);
            outputScrollView.setVisibility(View.VISIBLE);

            // 色を変える（他のボタンをデフォルトに戻す）
            summaryDisplayButton.setBackgroundColor(getResources().getColor(R.color.highlight_summary));
            allDisplayButton.setBackgroundColor(getResources().getColor(R.color.default_button));

            isSummaryDisplayVisible = true;
            isAllDisplayVisible = false;
        }
    }






}
