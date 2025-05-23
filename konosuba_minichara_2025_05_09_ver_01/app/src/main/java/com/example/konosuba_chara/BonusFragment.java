package com.example.konosuba_chara;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class BonusFragment extends Fragment {

    private BonusSession currentSession = new BonusSession(); // Fragment全体で保持するセッション
    private int bonusCount = 0; // ボーナス回数

    public BonusFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bonus, container, false);

        // SharedPreferences 取得
        SharedPreferences prefs = requireContext().getSharedPreferences("bonus_prefs", Context.MODE_PRIVATE);
        // フラグ読み込み：開始モードかどうか
        boolean isStartMode = prefs.getBoolean("isStartMode", true);

        // UI部品の取得
        Button startBonusButton = view.findViewById(R.id.startBonusButton);
        Button saveBonusButton = view.findViewById(R.id.saveBonusButton);
        Button resetBonusButton = view.findViewById(R.id.resetBonusButton);
        Button newSessionButton = view.findViewById(R.id.newSessionButton);



        EditText rotationEditText = view.findViewById(R.id.rotationEditText);
        AutoCompleteTextView triggerInput = view.findViewById(R.id.triggerInput);
        EditText triggerCountEditText = view.findViewById(R.id.triggerCountEditText);
        Spinner bonusTypeSpinner = view.findViewById(R.id.bonusTypeSpinner);
        EditText noteEditText = view.findViewById(R.id.noteEditText);

//        startBonusButton.setVisibility(isStartMode ? View.VISIBLE : View.GONE);
//        saveBonusButton.setVisibility(isStartMode ? View.GONE : View.VISIBLE);

        if (isStartMode) {
            startBonusButton.setVisibility(View.VISIBLE);
            saveBonusButton.setVisibility(View.GONE);
            newSessionButton.setVisibility(View.GONE);
        } else {
            startBonusButton.setVisibility(View.GONE);
            saveBonusButton.setVisibility(View.VISIBLE);
            newSessionButton.setVisibility(View.GONE); // 直近セッションが進行中なら非表示に
        }

        // ボーナス件数カウント（SharedPreferencesに保存しても良い）
        final int[] bonusCount = {0};
        final int[] startGames = {0};

        // ✅ 契機（AutoCompleteTextView）初期化
        String[] triggerOptions = {"チャンス目", "🍒", "🍉", "リーチ目", "単独"};
        ArrayAdapter<String> triggerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                triggerOptions
        );
        triggerInput.setAdapter(triggerAdapter);
        triggerInput.setThreshold(0); // 入力なしでも候補表示
        triggerInput.setOnClickListener(v -> triggerInput.showDropDown());

        // ✅ ボーナス種類（Spinner）初期化
        String[] bonusOptions = {
                "赤同色", "白同色", "黄同色", "白異色", "黄異色",
                "白REG", "黄REG",
                "真女神ぼーなす(白REG)", "真女神ぼーなす(黄REG)",
                "駄女神ぼーなす(白REG)", "駄女神ぼーなす(黄REG)"
        };
        ArrayAdapter<String> bonusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                bonusOptions
        );
        bonusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bonusTypeSpinner.setAdapter(bonusAdapter);


        // 保存済みデータの読み込み
        rotationEditText.setText(prefs.getString("rotation", ""));
        triggerInput.setText(prefs.getString("trigger", ""));
        triggerCountEditText.setText(prefs.getString("triggerCount", ""));
        noteEditText.setText(prefs.getString("note", ""));
        bonusTypeSpinner.setSelection(prefs.getInt("bonusTypePosition", 0));

        // テキスト変更時に自動保存するリスナー
        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("rotation", rotationEditText.getText().toString());
                editor.putString("trigger", triggerInput.getText().toString());
                editor.putString("triggerCount", triggerCountEditText.getText().toString());
                editor.putString("note", noteEditText.getText().toString());
                editor.apply();
            }
        };

        rotationEditText.addTextChangedListener(watcher);
        triggerInput.addTextChangedListener(watcher);
        triggerCountEditText.addTextChangedListener(watcher);
        noteEditText.addTextChangedListener(watcher);

        // Spinnerの選択変更も保存
        bonusTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedView, int position, long id) {
                prefs.edit().putInt("bonusTypePosition", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- 「開始」ボタン処理 ---
        startBonusButton.setOnClickListener(v -> {
            String rotationStr = rotationEditText.getText().toString().trim();
            String triggerStr = triggerInput.getText().toString().trim();
            String triggerCountStr = triggerCountEditText.getText().toString().trim();

            if (rotationStr.isEmpty()) {
                Toast.makeText(requireContext(), "回転数を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            int rotation = Integer.parseInt(rotationStr);
            int startGame = 0;

            if (triggerStr.isEmpty() && triggerCountStr.isEmpty()) {
                // ✅ 開始ゲーム数として扱う（0以外なら記録）
                startGame = rotation;
                Toast.makeText(requireContext(), "開始ゲーム数を記録しました", Toast.LENGTH_SHORT).show();
            } else {
                // ✅ 0Gスタート（ボーナス1件目として扱う）
                startGame = 0;
                Toast.makeText(requireContext(), "1件目のボーナスを記録しました", Toast.LENGTH_SHORT).show();
                // ※ 書き込みは今後
            }

            // ✅ ファイルを作成してタイトル行などを書き込み
            BonusFileUtil.createBonusFileWithHeader(requireContext(), startGame);

            // ✅ vol 番号を記憶（必要であれば）
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isStartMode", false);
            editor.apply();



//            if (triggerStr.isEmpty() && triggerCountStr.isEmpty()) {
//                // ✅ 開始回転数として扱う
//                startGame[0] = rotation;
//
//                System.out.println("*****************************************************");
//                System.out.println("startGame[0] : " + startGame[0]);
//                System.out.println("*****************************************************");
//                System.out.println("*****************************************************");
//                System.out.println("isStartMode : " + isStartMode);
//                System.out.println("*****************************************************");
//
//                Toast.makeText(requireContext(), "開始回転数を記録しました", Toast.LENGTH_SHORT).show();
//            } else {
//                // ✅ 0Gスタートとして1件目のボーナスとして書き込み
//                startGame[0] = 0;
//                bonusCount[0]++;
//
//                System.out.println("*****************************************************");
//                System.out.println("isStartMode : " + isStartMode);
//                System.out.println("*****************************************************");
//
//
//                // TODO: 書き込み処理をここに入れる（別メソッドに）
//                Toast.makeText(requireContext(), "1件目のボーナスを記録しました", Toast.LENGTH_SHORT).show();
//            }

            // モード切替＆保存
            prefs.edit().putBoolean("isStartMode", false).apply();
            startBonusButton.setVisibility(View.GONE);
            saveBonusButton.setVisibility(View.VISIBLE);
        });


        // --- 「保存」ボタン処理 ---
        saveBonusButton.setOnClickListener(v -> {
            String rotationStr = rotationEditText.getText().toString().trim();
            String triggerStr = triggerInput.getText().toString().trim();
            String triggerCountStr = triggerCountEditText.getText().toString().trim();
            String bonusType = bonusTypeSpinner.getSelectedItem().toString();
            String note = noteEditText.getText().toString().trim();

            System.out.println("*****************************************************");
            Log.d("BonusSave", "note = [" + note + "]");
            System.out.println("*****************************************************");

            // ✅ 「真女神ぼーなす」を含まない場合だけチェック
            if (!bonusType.contains("真女神ぼーなす")) {
                if (rotationStr.isEmpty() || triggerStr.isEmpty()) {
                    Toast.makeText(requireContext(), "ゲーム数と契機を入力してください", Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            if (rotationStr.isEmpty()) {
                Toast.makeText(requireContext(), "回転数を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

//            int game = Integer.parseInt(rotationStr);
            int game = rotationStr.isEmpty() ? 0 : Integer.parseInt(rotationStr);


            // BonusEntry 作成
            BonusEntry entry = new BonusEntry();
            entry.setGame(game);
            entry.setTrigger(triggerStr);
            entry.setTriggerCount(triggerCountStr);
            entry.setBonusType(bonusType);
            entry.setNote(note);

            System.out.println("*****************************************************");
            System.out.println("entry : " + entry);
            System.out.println("*****************************************************");


            currentSession.getBonusList().add(entry);
            Log.d("BonusFragment", "ボーナス " + currentSession.getBonusList().size() + " 件目を追加");
            currentSession.getBonusList().add(entry);

            Log.d("BonusFragment", "ボーナス " + bonusCount + " 件目を追加: " + entry.format());

            // ✅ セッションに追加
            if (currentSession == null) {
                currentSession = new BonusSession(game);  // 開始G指定
            }
            currentSession.addEntry(entry);

            // ✅ テキストファイルに追記
            writeBonusEntryToFile(requireContext(), entry);





            // 入力欄クリア
            rotationEditText.setText("");
            triggerInput.setText("");
            triggerCountEditText.setText("");
            noteEditText.setText("");
            bonusTypeSpinner.setSelection(0);

            // SharedPreferencesもクリア（任意）
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("rotation");
            editor.remove("trigger");
            editor.remove("triggerCount");
            editor.remove("note");
            editor.putInt("bonusTypePosition", 0);
            editor.apply();
        });




        // リセットボタンの取得と処理
        resetBonusButton.setOnClickListener(v -> {
            // 入力欄の初期化
            rotationEditText.setText("");
            triggerInput.setText("");
            triggerCountEditText.setText("");
            noteEditText.setText("");
            bonusTypeSpinner.setSelection(0); // 最初の項目に戻す

            // SharedPreferencesの内容もクリア
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // トースト表示
            Toast.makeText(requireContext(), "入力内容をリセットしました", Toast.LENGTH_SHORT).show();
        });

        newSessionButton.setOnClickListener(v -> {
            // 入力初期化
            rotationEditText.setText("");
            triggerInput.setText("");
            triggerCountEditText.setText("");
            noteEditText.setText("");
            bonusTypeSpinner.setSelection(0);

            // モードを「開始モード」に戻す
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();  // すべてリセット（必要であれば）
            editor.putBoolean("isStartMode", true);
            editor.apply();

            // ボタンの状態更新
            newSessionButton.setVisibility(View.GONE);
            startBonusButton.setVisibility(View.VISIBLE);
            resetBonusButton.setVisibility(View.VISIBLE);
            saveBonusButton.setVisibility(View.GONE);

            Toast.makeText(requireContext(), "新しいセッションを開始できます", Toast.LENGTH_SHORT).show();
        });



        Button showButton = view.findViewById(R.id.showBonusFileButton);
        TextView contentTextView = view.findViewById(R.id.bonusFileContentTextView);

        showButton.setOnClickListener(v -> {
            File file = BonusFileUtil.getCurrentFile(requireContext());
            if (file.exists()) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    contentTextView.setText(content.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "読み込み失敗", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "ファイルが存在しません", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    private void writeBonusEntryToFile(Context context, BonusEntry entry) {
        File file = BonusFileUtil.getCurrentFile(context);

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