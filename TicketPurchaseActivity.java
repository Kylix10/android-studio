package com.example.summer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TicketPurchaseActivity extends AppCompatActivity {

    // UI elements
    private TextView tvSelectedDate, tvTotalPrice;
    private TextView tvQuantity1, tvQuantity2, tvQuantity3;
    private Button btnSubmitOrder, btnBuyETicket;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Ticket prices
    private static final double PRICE_ADULT = 130.0;
    private static final double PRICE_STUDENT = 65.0;
    private static final double PRICE_BUTALA = 80.0;

    // Ticket quantities
    private int quantityAdult = 0;
    private int quantityStudent = 0; 
    private int quantityButala = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // 记录调试信息
            Log.d("TicketPurchase", "开始初始化购票页面");
            
            setContentView(R.layout.activity_ticket_purchase);
            Log.d("TicketPurchase", "设置内容视图成功");

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar == null) {
                Log.e("TicketPurchase", "找不到toolbar，请检查布局文件");
                Toast.makeText(this, "找不到toolbar组件", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("TicketPurchase", "找到toolbar");
            
            setSupportActionBar(toolbar);
            Log.d("TicketPurchase", "设置ActionBar成功");
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                Log.d("TicketPurchase", "配置ActionBar成功");
            } else {
                Log.e("TicketPurchase", "getSupportActionBar返回null");
            }

            // Initialize UI elements
            Log.d("TicketPurchase", "开始初始化UI元素");
            initializeViews();
            Log.d("TicketPurchase", "初始化视图完成");
            
            setupTabLayout();
            Log.d("TicketPurchase", "设置TabLayout完成");
            
            setupDatePicker();
            Log.d("TicketPurchase", "设置日期选择器完成");
            
            setupTicketCounters();
            Log.d("TicketPurchase", "设置票数计数器完成");
            
            setupButtons();
            Log.d("TicketPurchase", "设置按钮完成");

            // Initial UI update
            updateTotalPrice();
            Log.d("TicketPurchase", "更新总价完成");
            
            tvSelectedDate.setText(dateFormat.format(selectedDate.getTime()));
            Log.d("TicketPurchase", "设置日期完成");
            
            Log.d("TicketPurchase", "购票页面初始化成功");
        } catch (Exception e) {
            Log.e("TicketPurchase", "初始化购票页面出错: " + e.getMessage(), e);
            String errorMsg = "初始化购票页面出错: " + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            // 安全退出
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Text views
            tvSelectedDate = findViewById(R.id.tvSelectedDate);
            tvTotalPrice = findViewById(R.id.tvTotalPrice);
            tvQuantity1 = findViewById(R.id.tvQuantity1);
            tvQuantity2 = findViewById(R.id.tvQuantity2);
            tvQuantity3 = findViewById(R.id.tvQuantity3);

            // Buttons
            btnSubmitOrder = findViewById(R.id.btnSubmitOrder);
            btnBuyETicket = findViewById(R.id.btnBuyETicket);
        } catch (Exception e) {
            Toast.makeText(this, "查找UI元素出错", Toast.LENGTH_SHORT).show();
            throw e; // 重新抛出异常给onCreate处理
        }
    }

    private void setupTabLayout() {
        try {
            TabLayout tabLayout = findViewById(R.id.ticketTabLayout);
            if (tabLayout != null) {
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        // In a real app, we'd show different content based on the selected tab
                        Toast.makeText(TicketPurchaseActivity.this, 
                                "选择了: " + tab.getText(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        // No action needed
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        // No action needed
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "初始化选项卡出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupDatePicker() {
        try {
            ImageButton btnCalendar = findViewById(R.id.btnCalendar);
            if (btnCalendar != null) {
                btnCalendar.setOnClickListener(v -> {
                    try {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                this,
                                (view, year, month, dayOfMonth) -> {
                                    selectedDate.set(year, month, dayOfMonth);
                                    tvSelectedDate.setText(dateFormat.format(selectedDate.getTime()));
                                },
                                selectedDate.get(Calendar.YEAR),
                                selectedDate.get(Calendar.MONTH),
                                selectedDate.get(Calendar.DAY_OF_MONTH)
                        );
                        
                        // Set minimum date to today
                        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                        
                        // Set maximum date (e.g., 3 months from now)
                        Calendar maxDate = Calendar.getInstance();
                        maxDate.add(Calendar.MONTH, 3);
                        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
                        
                        datePickerDialog.show();
                    } catch (Exception e) {
                        Toast.makeText(TicketPurchaseActivity.this, "打开日期选择器失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "初始化日期选择器出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupTicketCounters() {
        try {
            // Ticket 1 - Adult
            View btnIncrease1 = findViewById(R.id.btnIncrease1);
            View btnDecrease1 = findViewById(R.id.btnDecrease1);
            
            if (btnIncrease1 != null) {
                btnIncrease1.setOnClickListener(v -> {
                    quantityAdult++;
                    tvQuantity1.setText(String.valueOf(quantityAdult));
                    updateTotalPrice();
                });
            }

            if (btnDecrease1 != null) {
                btnDecrease1.setOnClickListener(v -> {
                    if (quantityAdult > 0) {
                        quantityAdult--;
                        tvQuantity1.setText(String.valueOf(quantityAdult));
                        updateTotalPrice();
                    }
                });
            }

            // Ticket 2 - Student
            View btnIncrease2 = findViewById(R.id.btnIncrease2);
            View btnDecrease2 = findViewById(R.id.btnDecrease2);
            
            if (btnIncrease2 != null) {
                btnIncrease2.setOnClickListener(v -> {
                    quantityStudent++;
                    tvQuantity2.setText(String.valueOf(quantityStudent));
                    updateTotalPrice();
                });
            }

            if (btnDecrease2 != null) {
                btnDecrease2.setOnClickListener(v -> {
                    if (quantityStudent > 0) {
                        quantityStudent--;
                        tvQuantity2.setText(String.valueOf(quantityStudent));
                        updateTotalPrice();
                    }
                });
            }

            // Ticket 3 - Butala
            View btnIncrease3 = findViewById(R.id.btnIncrease3);
            View btnDecrease3 = findViewById(R.id.btnDecrease3);
            
            if (btnIncrease3 != null) {
                btnIncrease3.setOnClickListener(v -> {
                    quantityButala++;
                    tvQuantity3.setText(String.valueOf(quantityButala));
                    updateTotalPrice();
                });
            }

            if (btnDecrease3 != null) {
                btnDecrease3.setOnClickListener(v -> {
                    if (quantityButala > 0) {
                        quantityButala--;
                        tvQuantity3.setText(String.valueOf(quantityButala));
                        updateTotalPrice();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "初始化票数选择器出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupButtons() {
        try {
            if (btnSubmitOrder != null) {
                btnSubmitOrder.setOnClickListener(v -> {
                    try {
                        if (quantityAdult + quantityStudent + quantityButala == 0) {
                            Toast.makeText(this, "请至少选择一张门票", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        String message = "订单已提交，总金额: ¥" + calculateTotalPrice() + 
                                "，游览日期: " + tvSelectedDate.getText();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        
                        // In a real application, we would navigate to a payment screen
                    } catch (Exception e) {
                        Toast.makeText(this, "提交订单失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }

            if (btnBuyETicket != null) {
                btnBuyETicket.setOnClickListener(v -> {
                    Toast.makeText(this, "请选择您需要的门票种类和数量", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "初始化按钮出错", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private double calculateTotalPrice() {
        try {
            return quantityAdult * PRICE_ADULT + 
                    quantityStudent * PRICE_STUDENT + 
                    quantityButala * PRICE_BUTALA;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private void updateTotalPrice() {
        try {
            double total = calculateTotalPrice();
            if (tvTotalPrice != null) {
                tvTotalPrice.setText(String.format(Locale.CHINA, "¥%.0f", total));
            }
            
            // Enable/disable submit button based on selection
            if (btnSubmitOrder != null) {
                btnSubmitOrder.setEnabled(total > 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 