package com.example.konosuba_chara;


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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        recyclerView.setAdapter(adapter);

        // カウント変更時の更新処理
        adapter.setOnCountChangedListener(totalCount -> {
            updateAllSummaries();
            adapter.saveCountsToPrefs(context);
        });

        // 登録ボタン
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            int totalCount = adapter.getTotalCount();

            if (totalCount == 0 || totalCount % 6 != 0) {
                Toast.makeText(context, "ミニキャラ出現総数が6の倍数になっていません。登録できません。", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Integer> registeredBigNumbers = FileUtil.getRegisteredBigNumbers(context);
            Collections.sort(registeredBigNumbers);

            int registeredBigCount = registeredBigNumbers.size();
            int totalBigCount = (totalCount + 5) / 6;
            int nextBigNumber = registeredBigCount + 1;

            if (registeredBigCount > totalBigCount) {
                Toast.makeText(context, "BIG回数が整合していません。登録できません。", Toast.LENGTH_SHORT).show();
                return;
            } else if (registeredBigCount == totalBigCount) {
                Toast.makeText(context, "同じBIG回数が存在するため、上書きします。", Toast.LENGTH_SHORT).show();
                FileUtil.overwriteBigDataToFile(context, totalBigCount, adapter.getCounts(), totalCount);
                Toast.makeText(context, "上書きが完了しました。", Toast.LENGTH_SHORT).show();
            } else {
                if (nextBigNumber != totalBigCount) {
                    Toast.makeText(context, "BIG回数が連続していません。登録できません。", Toast.LENGTH_SHORT).show();
                    return;
                }
                FileUtil.overwriteBigDataToFile(context, nextBigNumber, adapter.getCounts(), totalCount);
                Toast.makeText(context, "登録が完了しました。", Toast.LENGTH_SHORT).show();
            }
        });

        // リセットボタン
        Button resetButton = view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> {
            FileUtil.incrementVol(context);

            int[] counts = adapter.getCounts();
            for (int i = 0; i < counts.length; i++) counts[i] = 0;

            updateAllSummaries();
            adapter.saveCountsToPrefs(context);
            adapter.notifyDataSetChanged();

            Toast.makeText(context, "データをリセットしました", Toast.LENGTH_SHORT).show();
        });

        // TODO: 全件表示・合計表示ボタンの処理も後で追加可能

        return view;
    }

    private void updateAllSummaries() {
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


        updateSummary(R.id.mainCount, R.id.mainRate, mainSum, totalCount);
        updateSummary(R.id.subCount, R.id.subRate, subSum, totalCount);
        updateSummary(R.id.succubusCount, R.id.succubusRate, succubus, totalCount);
        updateSummary(R.id.othersCount, R.id.othersRate, others, totalCount);

        int bigCount = (totalCount + 5) / 6;
        bigCountTextView.setText("BIG回数：" + bigCount + "回");
    }

    private void updateSummary(int countViewId, int rateViewId, int count, int total) {
        TextView countView = requireView().findViewById(countViewId);
        TextView rateView = requireView().findViewById(rateViewId);
        countView.setText(count + "回");

        double rate = total == 0 ? 0 : (double) count * 100 / total;
        String formatted = String.format(Locale.JAPAN, "%.2f%%", rate);
        rateView.setText(formatted);
    }
}
