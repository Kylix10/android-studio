package com.example.summer.ui.notifications;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.summer.R;

public class PersonalInfoActivity extends AppCompatActivity {
    private EditText editName, editGender, editBirthday;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // 启用顶部返回箭头
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 显示返回按钮
            getSupportActionBar().setTitle("个人信息"); // 设置标题
        }

        editName = findViewById(R.id.edit_name);
        editGender = findViewById(R.id.edit_gender);
        editBirthday = findViewById(R.id.edit_birthday);
        saveButton = findViewById(R.id.btn_save);

        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        editName.setText(prefs.getString("name", ""));
        editGender.setText(prefs.getString("gender", ""));
        editBirthday.setText(prefs.getString("birthday", ""));

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name", editName.getText().toString().trim());
            editor.putString("gender", editGender.getText().toString().trim());
            editor.putString("birthday", editBirthday.getText().toString().trim());
            editor.apply();
            Toast.makeText(this, "信息已保存", Toast.LENGTH_SHORT).show();
        });
    }
    // 响应返回按钮点击事件
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // 返回上一页
        return true;
    }
}
