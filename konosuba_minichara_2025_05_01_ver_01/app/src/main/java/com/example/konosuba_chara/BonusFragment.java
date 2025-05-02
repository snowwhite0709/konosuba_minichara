package com.example.konosuba_chara;



import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

public class BonusFragment extends Fragment {

    public BonusFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bonus, container, false);

        AutoCompleteTextView triggerInput = view.findViewById(R.id.triggerInput);

        // 契機の候補リスト（選択も可・手入力も可）
        String[] triggerOptions = {"チャンス目", "🍒", "🍉", "リーチ目", "単独"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                triggerOptions
        );

        // 先にアダプターを設定
        triggerInput.setAdapter(adapter);

        // 入力なしでも候補を表示できるように（タップで出す）
        triggerInput.setThreshold(0);

        // タップ時にドロップダウンを表示
        triggerInput.setOnClickListener(v -> triggerInput.showDropDown());


        Spinner bonusTypeSpinner = view.findViewById(R.id.bonusTypeSpinner);

        // ボーナスの種類（完全選択制）
        String[] bonusOptions = {
                "赤同色",
                "白同色",
                "黄同色",
                "白異色",
                "黄異色",
                "白REG",
                "黄REG",
                "真女神ぼーなす(白REG)",
                "真女神ぼーなす(黄REG)",
                "駄女神ぼーなす(白REG)",
                "駄女神ぼーなす(黄REG)"
        };

        ArrayAdapter<String> bonusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                bonusOptions
        );
        bonusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bonusTypeSpinner.setAdapter(bonusAdapter);


        return view;
    }
}