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
import android.view.inputmethod.InputMethodManager;
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


import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.*;

public class BonusFragment extends Fragment {

    private BonusSession currentSession = new BonusSession(); // Fragment全体で保持するセッション
    // private int bonusCount = 0; // ボーナス回数

    public BonusFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_bonus, container, false);
        // SharedPreferences 取得
        SharedPreferences prefs = requireContext().getSharedPreferences("bonus_prefs", Context.MODE_PRIVATE);
        // フラグ読み込み：開始モードかどうか
        boolean isStartMode = prefs.getBoolean(BonusPrefsKeys.IS_START_MODE, true);

        // UIコンポーネント取得
        Button startBonusButton = view.findViewById(R.id.startBonusButton);
        Button saveBonusButton = view.findViewById(R.id.saveBonusButton);
        Button resetBonusButton = view.findViewById(R.id.resetBonusButton);
        Button newSessionButton = view.findViewById(R.id.newSessionButton);
        Button showButton = view.findViewById(R.id.showBonusFileButton);
        TextView contentTextView = view.findViewById(R.id.bonusFileContentTextView);

        EditText rotationEditText = view.findViewById(R.id.rotationEditText);
        AutoCompleteTextView triggerInput = view.findViewById(R.id.triggerInput);
        EditText triggerCountEditText = view.findViewById(R.id.triggerCountEditText);
        Spinner bonusTypeSpinner = view.findViewById(R.id.bonusTypeSpinner);
        EditText noteEditText = view.findViewById(R.id.noteEditText);

        EditText editableContentTextView = view.findViewById(R.id.editableContentTextView);
        Button toggleEditButton = view.findViewById(R.id.toggleEditButton);
        Button saveEditButton = view.findViewById(R.id.saveEditButton);

        File file = BonusFileUtil.getCurrentFile(requireContext());
        StringBuilder originalContent = new StringBuilder();  // 編集前の内容保持用


        File currentFile = BonusFileUtil.getCurrentFile(requireContext());
        boolean fileExists = currentFile.exists();

        // 初期表示：読み取り専用モード
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    originalContent.append(line).append("\n");
                }
                editableContentTextView.setText(originalContent.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ファイルが存在しないのに isStartMode が false の場合は強制的に開始モードに戻す
        if (!fileExists && !isStartMode) {
            prefs.edit().putBoolean(BonusPrefsKeys.IS_START_MODE, true).apply();
            isStartMode = true;
        }


        // モードに応じて「開始」ボタン表示切替
        updateButtonStates(isStartMode, startBonusButton, saveBonusButton, newSessionButton);


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
                saveInputsToPrefs(prefs, rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);

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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // --- 「開始」ボタン処理：セッション開始（開始Gか1件目記録）＆ファイル作成 ---
        startBonusButton.setOnClickListener(v -> {
            String rotationStr = rotationEditText.getText().toString().trim();
            String triggerStr = triggerInput.getText().toString().trim();
            String triggerCountStr = triggerCountEditText.getText().toString().trim();

            if (rotationStr.isEmpty()) {
                Toast.makeText(requireContext(), "回転数を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            int startGame = Integer.parseInt(rotationStr);

            // 🔸 開始ゲーム数として記録（契機・契機回数が空なら）
            if (triggerStr.isEmpty() && triggerCountStr.isEmpty()) {
                // ✅ ファイルを作成してタイトル行などを書き込み
                BonusFileUtil.createBonusFileWithHeader(requireContext(), startGame);
                Toast.makeText(requireContext(), "開始ゲーム数を記録しました", Toast.LENGTH_SHORT).show();
            } else {
                // 🔸 開始ゲーム数 = 0 として、1件目のボーナスを記録
                BonusFileUtil.createBonusFileWithHeader(requireContext(), 0);
                // ✅ 0Gスタート（ボーナス1件目として扱う）
                Toast.makeText(requireContext(), "1件目のボーナスを記録しました", Toast.LENGTH_SHORT).show();

                // 🔸 BonusEntry を取得（バリデーション含む）
                BonusEntry entry = getBonusEntryFromInputs(requireContext(),
                        rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);

                if (entry == null) return;  // 入力に不備がある

                // ✅ セッションがなければ作成
                // ✅ テキストファイルに追記
                saveBonusEntry(requireContext(), entry);
            }

            // ✅ 入力内容のリセット（← ここが今回の追加処理）
            clearInputFields(rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);

            // ✅ vol 番号を記憶（必要であれば）
            // モード切替＆保存
            prefs.edit().putBoolean(BonusPrefsKeys.IS_START_MODE, false).apply();
            updateButtonStates(false, startBonusButton, saveBonusButton, newSessionButton);

        });


        // --- 「保存」ボタン処理：BonusEntry作成→セッションに追加→ファイル書き込み ---
        saveBonusButton.setOnClickListener(v -> {

            BonusEntry entry = getBonusEntryFromInputs(requireContext(),
                    rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);

            if (entry == null) return;  // バリデーションNG

            // ✅ セッションに追加（初回のみ new する）
            // ✅ テキストファイルに追記
            saveBonusEntry(requireContext(), entry); // ✅ 共通処理に集約

            // 入力欄クリア
            clearInputFields(rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);

            // saveInputsToPrefs(prefs, rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);
        });


        // リセットボタンの取得と処理：入力欄とSharedPreferencesクリア
        resetBonusButton.setOnClickListener(v -> {
            // 入力欄の初期化
            clearInputFields(rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);
            prefs.edit().clear().apply();
            Toast.makeText(requireContext(), "入力内容をリセットしました", Toast.LENGTH_SHORT).show();
        });

        // 「新規」ボタン：セッション初期化と前回ファイルの保存
        newSessionButton.setOnClickListener(v -> {
            Context context = requireContext();

            // 🔸現在のファイルを Downloads に保存（共通処理）
            saveCurrentFileToDownloads(context);

            // 🔸vol番号を進めて次のファイルに備える（＝永久欠番方式）
            BonusFileUtil.incrementVol(context);

            // 🔸入力欄を初期化s
            // 🔸SharedPreferences の状態を「開始モード」に戻す
            // 🔸ボタン表示状態の変更
            resetPrefsAndUI(prefs, rotationEditText, triggerInput, triggerCountEditText,
                    bonusTypeSpinner, noteEditText,
                    startBonusButton, saveBonusButton, resetBonusButton, newSessionButton);

            Toast.makeText(context, "新しいセッションを開始できます", Toast.LENGTH_SHORT).show();
        });


        // 「表示/非表示」ボタン処理
        contentTextView.setVisibility(View.GONE);
        editableContentTextView.setVisibility(View.GONE);
        toggleEditButton.setVisibility(View.GONE);
        saveEditButton.setVisibility(View.GONE);
        showButton.setText("表示");

        showButton.setOnClickListener(v -> {
            if (contentTextView.getVisibility() == View.GONE) {
                // 表示モード
                File file_showButton = BonusFileUtil.getCurrentFile(requireContext());
                if (file_showButton.exists()) {
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        String fileContent = content.toString();

                        contentTextView.setText(content.toString());
                        fadeView(contentTextView, true); // 🔽 フェード表示
                        // 🔽 編集用EditTextにも反映（次の編集に備える）
                        editableContentTextView.setText(fileContent);
                        editableContentTextView.setVisibility(View.GONE); // 編集欄はまだ非表示

                        // ボタン表示切り替え
                        toggleEditButton.setVisibility(View.VISIBLE);  // 編集ボタン表示
                        saveEditButton.setVisibility(View.GONE);       // 保存ボタン非表示
                        showButton.setText("非表示");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // 非表示モード
                fadeView(contentTextView, false); // 🔽 フェード非表示
                toggleEditButton.setVisibility(View.GONE); // 編集ボタンも非表示に
                editableContentTextView.setVisibility(View.GONE);
                saveEditButton.setVisibility(View.GONE);
                showButton.setText("表示");
            }
        });

        // モード切り替え：表示 ↔ 編集
        toggleEditButton.setOnClickListener(v -> {
            // 表示中の内容を編集欄にコピー
            String currentContent = contentTextView.getText().toString();
            editableContentTextView.setText(currentContent);
            // 🔽 編集欄を表示
            editableContentTextView.setVisibility(View.VISIBLE);
            // 編集可能状態に変更
            editableContentTextView.setEnabled(true);
            editableContentTextView.setFocusable(true);
            editableContentTextView.setFocusableInTouchMode(true);
            editableContentTextView.setCursorVisible(true);
            editableContentTextView.requestFocus();
            // キーボード表示
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editableContentTextView, InputMethodManager.SHOW_IMPLICIT);
            // 表示切り替え
            toggleEditButton.setVisibility(View.GONE);
            saveEditButton.setVisibility(View.VISIBLE);
        });

        // 保存処理
        saveEditButton.setOnClickListener(v -> {
            String updatedText = editableContentTextView.getText().toString();
            File file_showEditButton = BonusFileUtil.getCurrentFile(requireContext());  // ← これを追加

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_showEditButton, false))) {
                writer.write(updatedText);
                Toast.makeText(requireContext(), "保存しました", Toast.LENGTH_SHORT).show();
                // 🔽 反映させる
                contentTextView.setText(updatedText);
                editableContentTextView.setText(updatedText);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "保存に失敗しました", Toast.LENGTH_SHORT).show();
                return;
            }

            // 表示モードに戻す
            editableContentTextView.setFocusable(false);
            editableContentTextView.setFocusableInTouchMode(false);
            editableContentTextView.setCursorVisible(false);
            editableContentTextView.setEnabled(false);

            toggleEditButton.setVisibility(View.VISIBLE);
            saveEditButton.setVisibility(View.GONE);
        });

        return view;
    }


    private abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }


    private void fadeView(View view, boolean show) {
        view.setAlpha(show ? 0f : 1f);
        view.setVisibility(View.VISIBLE); // まず表示状態に

        view.animate()
                .alpha(show ? 1f : 0f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (!show) {
                        view.setVisibility(View.GONE); // フェードアウト後に非表示へ
                    }
                })
                .start();
    }

    private void clearInputFields(EditText rotationEditText,
                                  AutoCompleteTextView triggerInput,
                                  EditText triggerCountEditText,
                                  Spinner bonusTypeSpinner,
                                  EditText noteEditText) {
        rotationEditText.setText("");
        triggerInput.setText("");
        triggerCountEditText.setText("");
        noteEditText.setText("");
        bonusTypeSpinner.setSelection(0);
    }

    private void saveInputsToPrefs(SharedPreferences prefs,
                                   EditText rotationEditText,
                                   AutoCompleteTextView triggerInput,
                                   EditText triggerCountEditText,
                                   Spinner bonusTypeSpinner,
                                   EditText noteEditText) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BonusPrefsKeys.ROTATION, rotationEditText.getText().toString());
        editor.putString(BonusPrefsKeys.TRIGGER, triggerInput.getText().toString());
        editor.putString(BonusPrefsKeys.TRIGGER_COUNT, triggerCountEditText.getText().toString());
        editor.putString(BonusPrefsKeys.NOTE, noteEditText.getText().toString());
        editor.putInt(BonusPrefsKeys.BONUS_TYPE_POSITION, bonusTypeSpinner.getSelectedItemPosition());
        editor.apply();
    }

    private void restoreInputsFromPrefs(SharedPreferences prefs,
                                        EditText rotationEditText,
                                        AutoCompleteTextView triggerInput,
                                        EditText triggerCountEditText,
                                        Spinner bonusTypeSpinner,
                                        EditText noteEditText) {
        rotationEditText.setText(prefs.getString(BonusPrefsKeys.ROTATION, ""));
        triggerInput.setText(prefs.getString(BonusPrefsKeys.TRIGGER, ""));
        triggerCountEditText.setText(prefs.getString(BonusPrefsKeys.TRIGGER_COUNT, ""));
        noteEditText.setText(prefs.getString(BonusPrefsKeys.NOTE, ""));
        bonusTypeSpinner.setSelection(prefs.getInt(BonusPrefsKeys.BONUS_TYPE_POSITION, 0));
    }


    private void resetPrefsAndUI(SharedPreferences prefs,
                                 EditText rotationEditText,
                                 AutoCompleteTextView triggerInput,
                                 EditText triggerCountEditText,
                                 Spinner bonusTypeSpinner,
                                 EditText noteEditText,
                                 Button startBonusButton,
                                 Button saveBonusButton,
                                 Button resetBonusButton,
                                 Button newSessionButton) {
        // 入力欄クリア
        clearInputFields(rotationEditText, triggerInput, triggerCountEditText, bonusTypeSpinner, noteEditText);

        // SharedPreferencesクリア ＋ 開始モードに設定
        prefs.edit().putBoolean(BonusPrefsKeys.IS_START_MODE, true).apply();
        // ボタン表示状態の変更
        updateButtonStates(true, startBonusButton, saveBonusButton, newSessionButton);
        resetBonusButton.setVisibility(View.VISIBLE);
    }

    private BonusEntry getBonusEntryFromInputs(Context context,
                                               EditText rotationEditText,
                                               AutoCompleteTextView triggerInput,
                                               EditText triggerCountEditText,
                                               Spinner bonusTypeSpinner,
                                               EditText noteEditText) {

        String rotationStr = rotationEditText.getText().toString().trim();
        String triggerStr = triggerInput.getText().toString().trim();
        String triggerCountStr = triggerCountEditText.getText().toString().trim();
        String bonusType = bonusTypeSpinner.getSelectedItem().toString();
        String note = noteEditText.getText().toString().trim();

        // ✅ 真女神ボーナス以外は回転数・契機が必須
        if (!bonusType.contains("真女神ぼーなす")) {
            if (rotationStr.isEmpty() || triggerStr.isEmpty()) {
                Toast.makeText(context, "回転数と契機を入力してください", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        // ✅ 回転数は空なら 0（または入力必須にしてもよい）
        int game = rotationStr.isEmpty() ? 0 : Integer.parseInt(rotationStr);

        // ✅ BonusEntry を生成
        BonusEntry entry = new BonusEntry();
        entry.setGame(game);
        entry.setTrigger(triggerStr);
        entry.setTriggerCount(triggerCountStr);
        entry.setBonusType(bonusType);
        entry.setNote(note);

        return entry;
    }


    private void saveBonusEntry(Context context, BonusEntry entry) {
        // セッションが null の場合は初期化
        if (currentSession == null) {
            currentSession = new BonusSession(entry.getGame());
        }

        currentSession.addEntry(entry);
        // 呼び出し側（Fragment内）
        BonusFileUtil.writeBonusEntryToFile(requireContext(), entry);

    }


    private void updateButtonStates(boolean isStartMode,
                                    Button startBonusButton,
                                    Button saveBonusButton,
                                    Button newSessionButton) {
        startBonusButton.setVisibility(isStartMode ? View.VISIBLE : View.GONE);
        saveBonusButton.setVisibility(isStartMode ? View.GONE : View.VISIBLE);
        newSessionButton.setVisibility(isStartMode ? View.GONE : View.VISIBLE);
    }


    private void saveCurrentFileToDownloads(Context context) {
        File outerFile = BonusFileUtil.getCurrentFile(context);

        if (!outerFile.exists()) {
            Toast.makeText(context, "保存対象のファイルが見つかりません", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String fileName = outerFile.getName();
            String mimeType = "text/plain";

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = context.getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            );

            if (uri != null) {
                try (
                        OutputStream out = context.getContentResolver().openOutputStream(uri);
                        InputStream in = new FileInputStream(outerFile)
                ) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    Toast.makeText(context, "Downloadsに保存しました", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "保存先のURI取得に失敗しました", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(context, "Android 10以上で対応しています", Toast.LENGTH_SHORT).show();
        }
    }


}