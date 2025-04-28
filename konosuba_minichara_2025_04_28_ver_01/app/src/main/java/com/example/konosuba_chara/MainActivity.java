package com.example.konosuba_chara;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;


/**
 * メイン画面：キャラ出現数のカウントとBIG回数の表示を行う画面
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Android 13以降の画面端までレイアウトを広げるAPI
        setContentView(R.layout.activity_main); // レイアウトファイル読み込み

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



}
