package com.example.konosuba_chara;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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
        // ViewPager2（画面切り替え）と TabLayout（タブ表示）の参照変数
        private ViewPager2 viewPager;
        private TabLayout tabLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main); // activity_main.xml を画面に表示（TabLayout + ViewPager2）

            // レイアウト内のタブとページ切り替え部分を取得
            tabLayout = findViewById(R.id.tabLayout);
            viewPager = findViewById(R.id.viewPager);

            // ViewPager2 に Fragment の切り替え処理を設定
            viewPager.setAdapter(new FragmentStateAdapter(this) {
                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    // 画面の切り替え順序：左タブ → 右タブの順に並ぶ
                    if (position == 0) {
                        return new BonusFragment();  // 左のタブ：「ボーナス入力」画面
                    } else {
                        return new SettingFragment();// 右のタブ：「設定判別」画面
                    }
                }

                @Override
                public int getItemCount() {
                    return 2;// ページ数は2つ（ボーナス入力 / 設定判別）
                }
            });

            // TabLayout にタブタイトルを設定し、ViewPager2 と連動させる
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0) {
                    tab.setText("ボーナス入力"); // 1つ目のタブ（左）
                } else {
                    tab.setText("設定判別");// 2つ目のタブ（右）
                }
            }).attach();// タブとページを結びつけて表示
        }

}