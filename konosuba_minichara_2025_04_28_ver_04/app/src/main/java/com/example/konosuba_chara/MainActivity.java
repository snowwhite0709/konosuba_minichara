package com.example.konosuba_chara;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * メイン画面：キャラ出現数のカウントとBIG回数の表示を行う画面
 */
public class MainActivity extends AppCompatActivity {

    // --- MainActivity.java ---

    // 追加フィールド（クラスの最初に追加）
    private String currentDate;       // yyyy_MM_dd形式の日付
    private int currentVol = 1;        // 現在のvol番号（初期値1）
    private String currentFileName;    // 現在のファイル名


    /**
     * アプリ起動時に呼び出される初期化処理
     * - 今日の日付を取得
     * - 既存ファイルをスキャンして、最大のvol番号を取得
     * - ファイル名を初期化
     */    private void initializeFileInfo() {
        // 今日の日付を取得
        currentDate = new SimpleDateFormat("yyyy_MM_dd", Locale.JAPAN).format(new Date());
        // 既存ファイルから最大vol番号を取得
        currentVol = getMaxVolNumberForToday();
        // 取得したvol番号でファイル名を更新
        updateCurrentFileName();
    }
    /**
     * 今日の日付に対応するファイルの中で、最大のvol番号を取得する。
     * なければ1を返す。
     */
    private int getMaxVolNumberForToday() {
        int maxVol = 0;
        File dir = getFilesDir(); // 内部ストレージのアプリフォルダ
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.startsWith("【A+このすばBIG中ぬいぐるみ】" + currentDate) && name.endsWith(".txt")) {
                        // 例：【A+このすばBIG中ぬいぐるみ】2025_04_28_vol_01.txt
                        int volIndex = name.indexOf("_vol_");
                        if (volIndex != -1) {
                            try {
                                String volPart = name.substring(volIndex + 5, name.length() - 4); // "_vol_"の後から".txt"の前まで
                                int volNum = Integer.parseInt(volPart);
                                if (volNum > maxVol) {
                                    maxVol = volNum;
                                }
                            } catch (NumberFormatException e) {
                                // 無視（異常ファイル名）
                            }
                        }
                    }
                }
            }
        }
        // ファイルがなかったら1を返す（vol01からスタート）
        return maxVol == 0 ? 1 : maxVol;
    }


    // ファイル名を更新する処理（currentDateとcurrentVolから作る）
    private void updateCurrentFileName() {
        currentFileName = "【A+このすばBIG中ぬいぐるみ】" + currentDate + "_vol_" + String.format("%02d", currentVol) + ".txt";
    }

    // リセット時にvolを+1してファイル名を更新する処理
    private void incrementVol() {
        currentVol = getMaxVolNumberForToday() + 1;
        updateCurrentFileName();
    }




    /**
     * 指定したBIG回目のデータを書き込む、または上書きする処理
     * - 指定したBIG回目がすでに存在すれば、その部分を上書き
     * - 存在しなければ、末尾に新しく追加
     * - 書き込み形式は、各キャラごとの回数と確率、およびまとめ情報（メイン/サブ/サキュバス/確定系）
     *
     * @param bigNumber  書き込むBIG回数
     * @param counts     キャラクターごとの出現回数配列
     * @param totalCount 出現キャラの総数
     */
    private void overwriteBigDataToFile(int bigNumber, int[] counts, int totalCount) {
        try {
            // 現在のファイルオブジェクトを取得
            File file = new File(getFilesDir(), currentFileName);

            List<String> lines = new ArrayList<>();

            // ファイルが存在するなら、すべて読み込んでリストに保持
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                reader.close();
            }

            // 書き込み対象のBIG回目がすでに存在するかチェック
            int bigIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equals("【BIG　" + bigNumber + "回目】")) {
                    bigIndex = i;
                    break;
                }
            }

            // 新しくBIG回目データブロックを作成
            List<String> newBigBlock = new ArrayList<>();
            newBigBlock.add("【BIG　" + bigNumber + "回目】");
            newBigBlock.add("");

            CharacterType[] characters = CharacterType.values();

            // 各グループごとの集計用変数
            int mainSum = 0;
            int subSum = 0;
            int succubusSum = 0;
            int othersSum = 0;

            // キャラ別の出現回数と確率を1行ずつ追加
            for (int i = 0; i < characters.length; i++) {
                double rate = totalCount == 0 ? 0 : (double) counts[i] * 100 / totalCount;
                String formatted = String.format(Locale.JAPAN, "%.2f", rate);

                // 例：アクア　： 3回 (50.00%)
                newBigBlock.add(
                        String.format(Locale.JAPAN, "%-8s：%2d回 (%6s%%)", characters[i].getJapaneseName(), counts[i], formatted)
                );

                // メインキャラ/サブキャラ/サキュバス/確定系の合計に振り分け
                switch (characters[i]) {
                    case AQUA:
                    case DARKNESS:
                    case MEGUMIN:
                        mainSum += counts[i];
                        break;
                    case WIZ:
                    case YUNYUN:
                    case CHRIS:
                        subSum += counts[i];
                        break;
                    case SUCCUBUS:
                        succubusSum += counts[i];
                        break;
                    case OTHERS:
                        othersSum += counts[i];
                        break;
                }
            }

            newBigBlock.add(""); // 空行を追加

            // メインキャラ〜確定系のまとめ行（回数と出現確率付き）
            newBigBlock.add(String.format(Locale.JAPAN, "メインキャラ：%2d回 (%6.2f%%)", mainSum, (totalCount == 0 ? 0 : (double) mainSum * 100 / totalCount)));
            newBigBlock.add(String.format(Locale.JAPAN, "サブキャラ　：%2d回 (%6.2f%%)", subSum, (totalCount == 0 ? 0 : (double) subSum * 100 / totalCount)));
            newBigBlock.add(String.format(Locale.JAPAN, "サキュバス　：%2d回 (%6.2f%%)", succubusSum, (totalCount == 0 ? 0 : (double) succubusSum * 100 / totalCount)));
            newBigBlock.add(String.format(Locale.JAPAN, "確定系　　　：%2d回 (%6.2f%%)", othersSum, (totalCount == 0 ? 0 : (double) othersSum * 100 / totalCount)));

            newBigBlock.add(""); // 空行を追加
            newBigBlock.add("----------------------------------------");
            newBigBlock.add("");

            // ファイル内に既存BIGが存在する場合は、既存ブロックを削除して差し替え
            if (bigIndex != -1) {
                int endIndex = bigIndex;
                while (endIndex < lines.size() && !lines.get(endIndex).equals("----------------------------------------")) {
                    endIndex++;
                }
                if (endIndex < lines.size()) {
                    endIndex++; // 区切り線も含めて消す
                }
                lines.subList(bigIndex, endIndex).clear();
                lines.addAll(bigIndex, newBigBlock);
            } else {
                // ファイルが空なら、タイトルヘッダを追加
                if (lines.isEmpty()) {
                    lines.add("【A+このすばBIG中ぬいぐるみ】" + currentDate + "_vol_" + String.format("%02d", currentVol));
                    lines.add("----------------------------------------");
                }
                lines.addAll(newBigBlock);
            }

            // 全体を書き戻して上書き保存
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Android 13以降の画面端までレイアウトを広げるAPI
        setContentView(R.layout.activity_main); // レイアウトファイル読み込み

        // 起動時またはリセット後に、今日の日付に対応する最新のvol番号を調べ、
        // 使用するファイル名を更新（ファイル名、vol番号のリセット）
        initializeFileInfo();

        // BIG回数を表示するTextViewの取得
        TextView bigCountTextView = findViewById(R.id.bigCountTextView);

        // キャラ選択リスト（RecyclerView）の設定：2列表示
        RecyclerView recyclerView = findViewById(R.id.characterRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Adapterにキャラリストを渡してセット
        CharacterAdapter adapter = new CharacterAdapter(CharacterType.values());
        adapter.loadCountsFromPrefs(this); // ← 起動時に読み込み
        recyclerView.setAdapter(adapter);

        // キャラ出現数が変更されたときの処理を登録
        adapter.setOnCountChangedListener(totalCount -> {
            // まとめて、メインキャラ・サブキャラなどの出現回数・確率・BIG回数を更新
            updateAllSummaries(adapter, bigCountTextView);

            // 出現回数をSharedPreferencesに保存
            adapter.saveCountsToPrefs(MainActivity.this);
        });

        // ステータスバーやナビゲーションバーの余白を考慮してレイアウト調整
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // アプリ起動直後に、保存された出現回数データを元に、
        // メインキャラ・サブキャラなどの出現回数・確率・BIG回数をまとめて表示更新
        updateAllSummaries(adapter, bigCountTextView);

        // 登録ボタンとリセットボタンを取得
        Button saveButton = findViewById(R.id.saveButton);
        Button resetButton = findViewById(R.id.resetButton);

        // 登録ボタンを押したときの処理（今は未定なので仮の動作）
        saveButton.setOnClickListener(v -> {
            int totalCount = adapter.getTotalCount();

            if (totalCount == 0 || totalCount % 6 != 0) {
                Toast.makeText(MainActivity.this, "ミニキャラ出現総数が6の倍数になっていません。登録できません。", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Integer> registeredBigNumbers = getRegisteredBigNumbers();
            Collections.sort(registeredBigNumbers);

            int registeredBigCount = registeredBigNumbers.size();  // すでに登録されているBIGの数
            int totalBigCount = (totalCount + 5) / 6;               // 出現総数から計算されるBIGの数（ここは+5で切り上げOK）

            int nextBigNumber = registeredBigCount + 1; // 本来登録すべきBIG番号

            if (registeredBigCount > totalBigCount) {
                // すでに登録されているBIGのほうが多い場合（おかしいのでエラー）
                Toast.makeText(MainActivity.this, "BIG回数が整合していないため、登録できません。", Toast.LENGTH_SHORT).show();
                return;
            } else if (registeredBigCount == totalBigCount) {
                // すでに同じ回数分登録済み → 上書きする！
                Toast.makeText(MainActivity.this, "同じBIG回数が存在するため、上書きします。", Toast.LENGTH_SHORT).show();

                int[] counts = adapter.getCounts();
                overwriteBigDataToFile(totalBigCount, counts, totalCount);

                Toast.makeText(MainActivity.this, "上書きが完了しました。", Toast.LENGTH_SHORT).show();
                return;
            } else {
                // 次に登録するBIG番号が正しいなら通常登録
                if (nextBigNumber != totalBigCount) {
                    Toast.makeText(MainActivity.this, "BIG回数が連続していません。登録できません。", Toast.LENGTH_SHORT).show();
                    return;
                }

                int[] counts = adapter.getCounts();
                overwriteBigDataToFile(nextBigNumber, counts, totalCount);

                Toast.makeText(MainActivity.this, "登録が完了しました。", Toast.LENGTH_SHORT).show();
            }
        });


        // リセットボタンを押したときの処理
        resetButton.setOnClickListener(v -> {
            incrementVol(); // volを+1してファイル名を切り替える


            // すべてのカウントを0にリセット
            int[] counts = adapter.getCounts();
            for (int i = 0; i < counts.length; i++) {
                counts[i] = 0;
            }

            // 画面を更新（まとめて更新）
            updateAllSummaries(adapter, bigCountTextView);

            // SharedPreferencesも初期化して保存
            adapter.saveCountsToPrefs(MainActivity.this);

            // RecyclerView全体も更新（個別カウント0になったので）
            adapter.notifyDataSetChanged();





            // リセット完了をトースト表示（任意）
            Toast.makeText(MainActivity.this, "データをリセットしました", Toast.LENGTH_SHORT).show();
        });
    }



    /**
     * キャラクターごとの出現回数をもとに、メインキャラ・サブキャラ・サキュバス・確定系の
     * 出現回数と出現確率をまとめて画面に反映し、BIG回数も更新する。
     *
     * @param adapter キャラ出現回数を管理しているアダプター
     * @param bigCountTextView BIG回数を表示するTextView
     */
    private void updateAllSummaries(CharacterAdapter adapter, TextView bigCountTextView) {
        // キャラごとの出現回数を取得
        int[] counts = adapter.getCounts();
        int totalCount = adapter.getTotalCount(); // 総出現回数を取得

        // メインキャラ・サブキャラ・サキュバス・確定系それぞれの出現数を集計
        int mainSum = 0, subSum = 0, succubus = 0, others = 0;
        for (int i = 0; i < counts.length; i++) {
            CharacterType type = CharacterType.values()[i];
            switch (type) {
                case AQUA:
                case MEGUMIN:
                case DARKNESS:
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

        // 各グループの出現回数と出現確率を画面に反映
        updateSummary(R.id.mainCount, R.id.mainRate, mainSum, totalCount);
        updateSummary(R.id.subCount, R.id.subRate, subSum, totalCount);
        updateSummary(R.id.succubusCount, R.id.succubusRate, succubus, totalCount);
        updateSummary(R.id.othersCount, R.id.othersRate, others, totalCount);

        // BIG回数（総出現数 ÷ 6 を切り上げ）を画面に反映
        int bigCount = (totalCount + 5) / 6;
        bigCountTextView.setText("BIG回数：" + bigCount + "回");
    }



    /**
     * 指定された出現回数と総出現数をもとに、出現回数と出現確率をTextViewに表示する。
     *
     * @param countViewId 出現回数を表示するTextViewのID
     * @param rateViewId 出現確率を表示するTextViewのID
     * @param count 個別グループ（例：メインキャラなど）の出現回数
     * @param total キャラクター全体の総出現回数
     */
    private void updateSummary(int countViewId, int rateViewId, int count, int total) {
        // 出現回数を表示
        TextView countView = findViewById(countViewId);
        countView.setText(count + "回");

        // 出現確率を計算して表示（総回数が0なら0％とする）
        double rate = total == 0 ? 0 : (double) count * 100 / total;
        String formatted = String.format(Locale.JAPAN, "%.2f%%", rate);

        TextView rateView = findViewById(rateViewId);
        rateView.setText(formatted);
    }



    /**
     * 現在のファイル内のBIG回数をチェックし、次に登録できるBIG番号を返す。
     * もし連続していない場合は-1を返す（登録不可）。
     */
    private List<Integer> getRegisteredBigNumbers() {
        List<Integer> bigNumbers = new ArrayList<>();
        System.out.println("*****************************************************");
        System.out.println("currentFileName in getRegisteredBigNumbers() : " + currentFileName);
        System.out.println("*****************************************************");

        try {
            File file = new File(getFilesDir(), currentFileName);

            if (!file.exists()) {
                return bigNumbers;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("【BIG　") && line.endsWith("回目】")) {
                    String numberPart = line.replace("【BIG　", "").replace("回目】", "").trim();
                    try {
                        int bigNum = Integer.parseInt(numberPart);
                        bigNumbers.add(bigNum);
                    } catch (NumberFormatException e) {
                        // 無視
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bigNumbers;
    }

    /**
     * 現在のファイル内で登録されている最大BIG番号を取得する。
     * ファイルがなければ0を返す。
     */
    private int getCurrentMaxBigNumber() {
        int maxBig = 0;

        try {
            File file = new File(getFilesDir(), currentFileName);

            if (!file.exists()) {
                return 0;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("【BIG　") && line.endsWith("回目】")) {
                    String numberPart = line.replace("【BIG　", "").replace("回目】", "").trim();
                    try {
                        int bigNum = Integer.parseInt(numberPart);
                        if (bigNum > maxBig) {
                            maxBig = bigNum;
                        }
                    } catch (NumberFormatException e) {
                        // 無視
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return maxBig;
    }


}
