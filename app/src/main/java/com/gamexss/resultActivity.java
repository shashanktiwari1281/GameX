package com.gamexss;

import static com.gamexss.MainActivity.getTime;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class resultActivity extends AppCompatActivity {
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private SharedPreferences userDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent=getIntent();
        ConstraintLayout constraintLayout=findViewById(R.id.constraintLayoutResult);
        userDetails= getSharedPreferences("userDetails", MODE_PRIVATE);
        TextView textView=findViewById(R.id.resultHead);
        ImageView imageView=findViewById(R.id.imgResultPg);
        findViewById(R.id.tryAgainBtnResultPg).setOnClickListener(v -> finish());
        int generatedNumber = intent.getIntExtra("generatedNumber",0);
        Dialog progressDialog=new Dialog(getApplicationContext());
        progressDialog.setContentView(R.layout.loading_panel);
        progressDialog.setCancelable(false);
        db.collection("userDetails").document(userDetails.getString("mobileNumber","NA"))
                .collection("basicUserDetails").document("balances")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        double profitBalance = Objects.requireNonNull(task.getResult().getDouble("profitBalance"));
                        double totalBalance = Objects.requireNonNull(task.getResult().getDouble("totalBalance"));
                        long matchNumber = Long.parseLong(intent.getStringExtra("matchNo"));
                        double investedAmount = Double.parseDouble(intent.getStringExtra("investedAmount"));
                        boolean condition = (matchNumber % 2 == 0 && investedAmount < totalBalance * 35 / 100) || (matchNumber % 2 != 0 && (investedAmount >= totalBalance * 35 / 100 && investedAmount < totalBalance * 70 / 100));
                        if (totalBalance>0) {
                            if (totalBalance - investedAmount >= profitBalance) {
                                totalBalance = totalBalance-investedAmount;
                            } else if (totalBalance == profitBalance) {
                                totalBalance =totalBalance-investedAmount;
                                profitBalance = totalBalance;
                            } else {
                                double temp = totalBalance - profitBalance;
                                investedAmount = investedAmount-temp;
                                profitBalance =profitBalance-investedAmount;
                                totalBalance = profitBalance;
                            }
                        }else{
                            Toast.makeText(this, "Something going wrong!1", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        if (intent.getBooleanExtra("investedOnNumber",false)&&generatedNumber == Integer.parseInt(intent.getStringExtra("investedOn")) && condition) {
                            Map<String, Object> data = new HashMap<>();
                            final double wonAmount = Double.parseDouble(new DecimalFormat("#.0").format(investedAmount * 3.8));
                            data.put("totalBalance", totalBalance + wonAmount);
                            data.put("profitBalance", profitBalance + wonAmount);
                            db.collection("userDetails").document(userDetails.getString("mobileNumber", "Blank")).collection("basicUserDetails").document("balances")
                                    .update(data)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            progressDialog.dismiss();
                                            constraintLayout.setBackgroundResource(R.drawable.win);
                                            String str = "Congratulations!\n You Won the Match\nRs. " + wonAmount + " added in your wallet.";
                                            textView.setText(str);
                                            textView.setTextColor(Color.GREEN);
                                            Map<String,Object> txnDataList=new HashMap<>();
                                            txnDataList.put("title","Debit from Wallet");
                                            txnDataList.put("desc","Invested in #"+matchNumber);
                                            txnDataList.put("status","Success");
                                            txnDataList.put("amount",intent.getStringExtra("investedAmount"));
                                            txnDataList.put("createdAt", intent.getStringExtra("createdAt"));
                                            txnDataList.put("timeStamp", FieldValue.serverTimestamp());
                                            db.collection("userDetails").document(userDetails.getString("mobileNumber","Blank")).collection("txnHistory").document().set(txnDataList);
                                            txnDataList.put("title","Credit in Wallet");
                                            txnDataList.put("desc","Won in #"+matchNumber);
                                            txnDataList.put("status","Success");
                                            txnDataList.put("amount",wonAmount);
                                            txnDataList.put("createdAt", getTime("dd MMM yyyy hh:mm a"));
                                            txnDataList.put("timeStamp", FieldValue.serverTimestamp());
                                            db.collection("userDetails").document(userDetails.getString("mobileNumber","Blank")).collection("txnHistory").document().set(txnDataList);
                                            Map<String,Object> winRecData=new HashMap<>();
                                            winRecData.put("generatedNo",generatedNumber);
                                            winRecData.put("color",intent.getStringExtra("generatedColor"));
                                            winRecData.put("wonAmount",wonAmount);
                                            winRecData.put("timeStamp", FieldValue.serverTimestamp());
                                            db.collection("earnRecList").document(String.valueOf(matchNumber)).set(winRecData);
                                        } else {
                                            Toast.makeText(this, "Something going wrong!2", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }else if (intent.getBooleanExtra("investedOnColor",false)&&Objects.equals(intent.getStringExtra("investedOn"), intent.getStringExtra("generatedColor")) && condition) {
                            Map<String, Object> data = new HashMap<>();
                            final double wonAmount = Double.parseDouble(new DecimalFormat("#.0").format(investedAmount * 1.9));
                            data.put("totalBalance", totalBalance + wonAmount);
                            data.put("profitBalance", profitBalance + wonAmount);
                            db.collection("userDetails").document(userDetails.getString("mobileNumber", "Blank")).collection("basicUserDetails").document("balances")
                                    .update(data)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            progressDialog.dismiss();
                                            constraintLayout.setBackgroundResource(R.drawable.win);
                                            String str = "Congratulations!\n You Won the Match\nRs. " + wonAmount + " added in your wallet.";
                                            textView.setText(str);
                                            textView.setTextColor(Color.GREEN);
                                            Map<String,Object> txnDataList=new HashMap<>();
                                            txnDataList.put("title","Debit from Wallet");
                                            txnDataList.put("desc","Invested in #"+matchNumber);
                                            txnDataList.put("status","Success");
                                            txnDataList.put("amount",intent.getStringExtra("investedAmount"));
                                            txnDataList.put("createdAt", intent.getStringExtra("createdAt"));
                                            txnDataList.put("timeStamp", FieldValue.serverTimestamp());
                                            db.collection("userDetails").document(userDetails.getString("mobileNumber","Blank")).collection("txnHistory").document().set(txnDataList);
                                            txnDataList.put("title","Credit in Wallet");
                                            txnDataList.put("desc","Won in #"+matchNumber);
                                            txnDataList.put("status","Success");
                                            txnDataList.put("amount",wonAmount);
                                            txnDataList.put("createdAt", getTime("dd MMM yyyy hh:mm a"));
                                            txnDataList.put("timeStamp", FieldValue.serverTimestamp());
                                            db.collection("userDetails").document(userDetails.getString("mobileNumber","Blank")).collection("txnHistory").document().set(txnDataList);
                                            Map<String,Object> winRecData=new HashMap<>();
                                            winRecData.put("generatedNo",generatedNumber);
                                            winRecData.put("color",intent.getStringExtra("generatedColor"));
                                            winRecData.put("wonAmount",wonAmount);
                                            db.collection("earnRecList").document(String.valueOf(matchNumber)).set(winRecData);
                                        } else {
                                            Toast.makeText(this, "Something going wrong!3", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }else {
                            Map<String, Object> data = new HashMap<>();
                            data.put("totalBalance", totalBalance);
                            data.put("profitBalance", profitBalance);
                            db.collection("userDetails").document(userDetails.getString("mobileNumber", "Blank")).collection("basicUserDetails").document("balances")
                                    .update(data)
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            progressDialog.dismiss();
                                            imageView.setImageResource(R.drawable.lose);
                                            String str = "You loose the Match!\nRs. " + intent.getStringExtra("investedAmount") + " deducted from your wallet.";
                                            textView.setText(str);
                                            textView.setTextColor(Color.RED);
                                            Map<String, Object> txnDataList = new HashMap<>();
                                            txnDataList.put("title", "Debit from Wallet");
                                            txnDataList.put("desc", "Invested in #" + matchNumber);
                                            txnDataList.put("status", "Success");
                                            txnDataList.put("amount", intent.getStringExtra("investedAmount"));
                                            txnDataList.put("createdAt", intent.getStringExtra("createdAt"));
                                            txnDataList.put("timeStamp", FieldValue.serverTimestamp());
                                            db.collection("userDetails").document(userDetails.getString("mobileNumber", "Blank")).collection("txnHistory").document().set(txnDataList);
                                        }else {
                                            Toast.makeText(this, "Something going wrong!4", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }
                    }else {
                        Toast.makeText(this, "Something going wrong!5", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}