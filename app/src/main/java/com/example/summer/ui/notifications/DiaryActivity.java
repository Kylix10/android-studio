package com.example.summer.ui.notifications;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.summer.R;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryActivity extends AppCompatActivity {
    private LinearLayout diaryContainer;
    private Button floatNoteBtn;
    private List<Diary> diaryList = new ArrayList<>();
    private static final String SHARED_PREFS_NAME = "diary_prefs";
    private static final String DIARY_LIST_KEY = "diary_list";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        // 启用顶部返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 显示返回按钮
            getSupportActionBar().setTitle("我的日记"); // 设置标题
        }

        diaryContainer = findViewById(R.id.diary_content_container);
        floatNoteBtn = findViewById(R.id.float_note_btn);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        // 从 SharedPreferences 加载日记数据
        loadDiaryDataFromSharedPrefs();

        // 加载日记内容
        loadDiaryContent();

        // 随手记按钮点击事件
        floatNoteBtn.setOnClickListener(v -> showNoteDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        // 保存日记数据到 SharedPreferences
        saveDiaryDataToSharedPrefs();
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 保存日记数据到 SharedPreferences
        saveDiaryDataToSharedPrefs();
    }

    private void loadDiaryDataFromSharedPrefs() {
        String diaryListJson = sharedPreferences.getString(DIARY_LIST_KEY, null);
        if (diaryListJson != null) {
            Gson gson = new Gson();
            diaryList = gson.fromJson(diaryListJson, new TypeToken<List<Diary>>() {}.getType());
        }
    }

    private void saveDiaryDataToSharedPrefs() {
        Gson gson = new Gson();
        String diaryListJson = gson.toJson(diaryList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DIARY_LIST_KEY, diaryListJson);
        editor.apply();
    }
    private void loadDiaryContent() {
        diaryContainer.removeAllViews();
        if (!diaryList.isEmpty()) {
            for (Diary diary : diaryList) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_diary, diaryContainer, false);
                TextView dateText = itemView.findViewById(R.id.diary_date);
                TextView contentText = itemView.findViewById(R.id.diary_content);
                dateText.setText(diary.getDate());
                contentText.setText(diary.getContent());
                diaryContainer.addView(itemView);
            }
        } else {
            // 无日记时可不做处理（布局中默认隐藏）
        }
    }

    private void showNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 关闭按钮（×）点击事件（保持不变：直接关闭弹窗，不保存）
        dialogView.findViewById(R.id.dialog_close).setOnClickListener(v -> dialog.dismiss());

        // 完成按钮点击事件（逻辑不变：保存内容并关闭）
        dialogView.findViewById(R.id.save_btn).setOnClickListener(v -> {
            EditText contentEdit = dialogView.findViewById(R.id.note_content);
            String content = contentEdit.getText().toString().trim();
            if (!content.isEmpty()) {
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
                diaryList.add(new Diary(currentDate, content));
                loadDiaryContent(); // 刷新日记展示
                // 保存日记数据到 SharedPreferences
                saveDiaryDataToSharedPrefs();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "日记内容不能为空", Toast.LENGTH_SHORT).show();
            }
        });

        // 移除取消按钮的相关代码（原取消按钮已删除，无需处理）
        // 原代码：dialogView.findViewById(R.id.cancel_btn).setOnClickListener(v -> dialog.dismiss());
    }

    // 日记数据类
    private static class Diary {
        private String date;
        private String content;

        public Diary(String date, String content) {
            this.date = date;
            this.content = content;
        }

        public String getDate() {
            return date;
        }

        public String getContent() {
            return content;
        }
    }
}