package com.example.konosuba_chara;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;


/**
 * キャラクター出現回数を表示・カウントするための RecyclerView Adapter
 */
public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {

    private final CharacterType[] characterList; // キャラクター一覧（Enum）
    private final int[] counts; // 各キャラの出現カウント
    private OnCountChangedListener listener; // カウント合計変更時の通知用リスナー



    public void saveCountsToPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("chara_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < counts.length; i++) {
            editor.putInt("count_" + characterList[i].name(), counts[i]);
        }

        editor.apply();
    }

    public void loadCountsFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("chara_prefs", Context.MODE_PRIVATE);

        for (int i = 0; i < counts.length; i++) {
            counts[i] = prefs.getInt("count_" + characterList[i].name(), 0);
        }

        notifyDataSetChanged(); // 表示を更新
    }


    /**
     * カウント合計が変更されたときに通知するためのリスナーインターフェース
     */
    public interface OnCountChangedListener {
        void onCountChanged(int totalCount);
    }

    /**
    *コンストラクタ：キャラ一覧を受け取り、出現回数を初期化
    */
    public CharacterAdapter(CharacterType[] characterList) {
        this.characterList = characterList;
        this.counts = new int[characterList.length];
    }
    /**
     * カウント変更を通知し、全体の再描画（出現確率を更新）を行う
     */
    private void notifyCountChanged() {
        if (listener != null) {
            listener.onCountChanged(getTotalCount());
        }
        notifyDataSetChanged(); // 全キャラの出現確率を再計算するため再描画

//        // 自動保存（contextが必要なので listener を介してもOK）
//        if (context != null) {
//            saveCountsToPrefs(context);
//        }

    }
    // リスナーの登録
    public void setOnCountChangedListener(OnCountChangedListener listener) {
        this.listener = listener;
    }

    /**
     * 全キャラのカウント合計を返す
     */
    public int getTotalCount() {
        int total = 0;
        for (int c : counts) total += c;
        return total;
    }






    // 各アイテム（キャラ）用の View を生成
    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.character_counter_item, parent, false);
        return new CharacterViewHolder(view);
    }

    /**
     * 各キャラごとの表示処理（名前・カウント・出現確率・ボタン処理）
     */
    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        CharacterType character = characterList[position];
        int count = counts[position];

        holder.characterNameTextView.setText(character.getJapaneseName());
        holder.countTextView.setText(String.valueOf(count));

        CharacterCategory category = character.getCategory();

        int bgColor;
        switch (category) {
            case MAIN:
                bgColor = Color.parseColor("#E3F2FD"); // 薄い青
                break;
            case SUB:
                bgColor = Color.parseColor("#FFEBEE"); // 薄い赤
                break;
            case SUCCUBUS:
                bgColor = Color.parseColor("#F3E5F5"); // 薄い紫
                break;
            case FIXED:
                bgColor = Color.parseColor("#FFF9C4"); // 金色っぽい
                break;
            default:
                bgColor = Color.parseColor("#EEEEEE"); // デフォルト色
        }

        holder.itemView.setBackgroundColor(bgColor);



        updateProbability(holder, position); // 初期確率表示

        // ＋ボタン押下時：カウント＋1し、TextViewとリスナーを更新
        holder.plusButton.setOnClickListener(v -> {
            counts[position]++;
            holder.countTextView.setText(String.valueOf(counts[position]));
            notifyCountChanged(); // カウント変化後はすべて更新（確率含む）
        });

        // −ボタン押下時：0より大きければカウント−1、TextViewとリスナーを更新
        holder.minusButton.setOnClickListener(v -> {
            if (counts[position] > 0) {
                counts[position]--;
                holder.countTextView.setText(String.valueOf(counts[position]));
                notifyCountChanged();// カウント変化後はすべて更新（確率含む）
            }
        });
    }

    /**
     * 指定キャラの出現確率を TextView に反映
     * 出現確率 = (各キャラの出現数 ÷ 総出現数) × 100
     */
    private void updateProbability(CharacterViewHolder holder, int position) {
        int total = getTotalCount();
        if (total == 0) {
            holder.probabilityTextView.setText("(0.00%)");
            return;
        }

        double rate = (double) counts[position] * 100 / total;
        String formatted = String.format(Locale.JAPAN, "(%.2f%%)", rate);
        holder.probabilityTextView.setText(formatted);
    }


    // アイテム数（キャラの数）を返す
    @Override
    public int getItemCount() {
        return characterList.length;
    }

    // カウント配列を外部から取得（保存や分析などに使用可）
    public int[] getCounts() {
        return counts;
    }




    /**
     * ViewHolderクラス：1キャラ分のViewを保持（ViewのfindViewByIdはここで行う）
     */
    static class CharacterViewHolder extends RecyclerView.ViewHolder {
        TextView characterNameTextView;
        TextView countTextView;
        Button plusButton;
        Button minusButton;
        TextView probabilityTextView;
        LinearLayout characterItemRoot; // ← 追加
        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            characterNameTextView = itemView.findViewById(R.id.characterNameTextView);
            countTextView = itemView.findViewById(R.id.countTextView);
            plusButton = itemView.findViewById(R.id.plusButton);
            minusButton = itemView.findViewById(R.id.minusButton);
            probabilityTextView = itemView.findViewById(R.id.probabilityTextView);
            characterItemRoot = itemView.findViewById(R.id.characterItemRoot); // ← 追加
        }
    }
}
