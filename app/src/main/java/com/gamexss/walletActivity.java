package com.gamexss;

import static com.gamexss.MainActivity.getTime;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class walletActivity extends AppCompatActivity {
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private SharedPreferences userDetails;
    private TextView withdrawableBalance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        findViewById(R.id.backBtnWalletPg).setOnClickListener(v -> finish());
        findViewById(R.id.profileViewWalletPg).setOnClickListener(v -> startActivity(new Intent(this, profileActivity.class)));
        TextView totalBalanceTV=findViewById(R.id.totalBalanceWalletPg);
                withdrawableBalance=findViewById(R.id.withdrawableAmountWalletPg);
        db.collection("userDetails")
                .document(userDetails.getString("mobileNumber","NA"))
                .collection("basicUserDetails")
                .document("balances")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double profitBalance= Objects.requireNonNull(task.getResult().getDouble("profitBalance"));
                        double totalBalance =Objects.requireNonNull(task.getResult().getDouble("totalBalance"));
                        totalBalanceTV.setText(new DecimalFormat("#.0").format(totalBalance));
                        if (totalBalance>9)totalBalanceTV.setTextColor(Color.GREEN);
                        else totalBalanceTV.setTextColor(Color.RED);
                        withdrawableBalance.setText(new DecimalFormat("#.0").format(profitBalance));
                        if (profitBalance>249)withdrawableBalance.setTextColor(Color.GREEN);
                        else withdrawableBalance.setTextColor(Color.RED);
                    } else {
                        Log.d("firebase3", "Error getting documents: ", task.getException());
                    }
                });
        findViewById(R.id.historyButtonWalletPg).setOnClickListener(v -> startActivity(new Intent(this, transactionHistory.class)));
        findViewById(R.id.withdrawButtonWalletPg).setOnClickListener(v -> withdrawDialog());
        findViewById(R.id.addMoneyWalletPg).setOnClickListener(v -> {});
        findViewById(R.id.posterWalletPg).setOnClickListener(v -> startActivity(new Intent(this, referEarnActivity.class)));
    }
    private boolean isViewingTC=false;
    public void withdrawDialog(){
        Dialog withdrawDialog=new Dialog(this);
        withdrawDialog.setContentView(R.layout.withdraw_dialog);
        withdrawDialog.setCanceledOnTouchOutside(false);
        TextView readWithdrawTC=withdrawDialog.findViewById(R.id.readWithdrawTC);
        readWithdrawTC.setOnClickListener(v -> {
            TextView withdrawTC=withdrawDialog.findViewById(R.id.withdrawTC);
            if(!isViewingTC){
                withdrawTC.setText(R.string.tc_of_withdraw);
                readWithdrawTC.setText(R.string.hide);
                isViewingTC=true;
            }
            else {
                withdrawTC.setText("");
                readWithdrawTC.setText(R.string.read_t_c);
                isViewingTC=false;
            }
        });
        EditText amountET=withdrawDialog.findViewById(R.id.withdrawingAmount);
        EditText upiIdET=withdrawDialog.findViewById(R.id.withdrawUpiId);
        upiIdET.setText(userDetails.getString("upiId",""));
        Button submitBtn=withdrawDialog.findViewById(R.id.withdrawRequestBtn);
        submitBtn.setOnClickListener(view1 -> {
            String amount = amountET.getText().toString(), upiId=upiIdET.getText().toString();
            double amountDouble=Double.parseDouble(amount);
            if (amount.equals("")) Toast.makeText(this, "Enter the Amount", Toast.LENGTH_SHORT).show();
            else if (!upiId.contains("@")) Toast.makeText(this, "Enter a valid receiver's UPI ID", Toast.LENGTH_SHORT).show();
            else if(Double.parseDouble(withdrawableBalance.getText().toString())<250||amountDouble<250) Toast.makeText(this, "Couldn't process.\nRead T&C", Toast.LENGTH_SHORT).show();
            else if(Double.parseDouble(withdrawableBalance.getText().toString())<amountDouble||amountDouble>1500) Toast.makeText(this, "Insufficient Balance, Enter lesser amount.", Toast.LENGTH_SHORT).show();
            else {
                Dialog progressDialog = new Dialog(this);
                progressDialog.setContentView(R.layout.loading_panel);
                progressDialog.setCancelable(false);
                progressDialog.show();
                Map<String, Object> dataList = new HashMap<>();
                dataList.put("createdAt", getTime("dd MMM yyyy hh:mm a"));
                if(amountDouble<300) dataList.put("amount",Double.parseDouble(new DecimalFormat("#.0").format(amountDouble-(amountDouble*20/100))));
                if(300<amountDouble&&amountDouble<700) dataList.put("amount",Double.parseDouble(new DecimalFormat("#.0").format(amountDouble-(amountDouble*3/100))));
                if(700<amountDouble&&amountDouble<1000) dataList.put("amount",Double.parseDouble(new DecimalFormat("#.0").format(amountDouble-(amountDouble*2/100))));
                if(1000<amountDouble) dataList.put("amount",Double.parseDouble(new DecimalFormat("#.0").format(amountDouble-(amountDouble*1/100))));
                dataList.put("upiId", upiId);
                dataList.put("timeStamp", FieldValue.serverTimestamp());
                db.collection("withdrawRequest").document(userDetails.getString("mobileNumber", "NA"))
                        .set(dataList)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                dataList.put("title","Debit from Wallet");
                                dataList.put("desc","Withdrawal to "+upiId);
                                dataList.put("status","Pending");
                                dataList.put("amount",amountDouble);
                                dataList.remove("upiId");
                                db.collection("userDetails").document(userDetails.getString("mobileNumber","Blank")).collection("txnHistory").document()
                                        .set(dataList)
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Request Submitted", Toast.LENGTH_SHORT).show();
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        withdrawDialog.show();
    }
}