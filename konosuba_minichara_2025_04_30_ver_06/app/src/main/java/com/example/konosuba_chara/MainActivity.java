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

        private ViewPager2 viewPager;
        private TabLayout tabLayout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main); // ← ここはタブホスト用のXML

            tabLayout = findViewById(R.id.tabLayout);
            viewPager = findViewById(R.id.viewPager);

            // ViewPager2 に Fragment を設定
            viewPager.setAdapter(new FragmentStateAdapter(this) {
                @NonNull
                @Override
                public Fragment createFragment(int position) {
                    if (position == 0) {
                        return new BonusFragment();  // ← こちらを先に
                    } else {
                        return new SettingFragment();
                    }
                }

                @Override
                public int getItemCount() {
                    return 2;
                }
            });

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0) {
                    tab.setText("ボーナス入力"); // ← 左のタブ
                } else {
                    tab.setText("設定判別");
                }
            }).attach();
        }

}