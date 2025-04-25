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
        recyclerView.setAdapter(adapter);

        // キャラ出現数が変化したとき、BIG回数の表示を更新
        adapter.setOnCountChangedListener(totalCount -> {
            int bigCount = (totalCount + 5) / 6; // 6で割って切り上げ（例：1～6→1回、7～12→2回）
            bigCountTextView.setText("BIG回数：" + bigCount + "回");
        });

        // 画面端の余白調整（元からある処理）
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // ステータスバーやナビゲーションバーの余白を考慮してレイアウト調整
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


}
