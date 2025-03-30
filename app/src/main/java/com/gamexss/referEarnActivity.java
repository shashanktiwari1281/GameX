package com.gamexss;

import static com.gamexss.MainActivity.getTime;
import static com.gamexss.MainActivity.isConnectionAvailable;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class referEarnActivity extends AppCompatActivity {
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private SharedPreferences userDetails;
    private double totalBalance=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_earn);
        userDetails= getSharedPreferences("userDetails", MODE_PRIVATE);
        findViewById(R.id.backBtnReferPg).setOnClickListener(v -> finish());
        TextView totalEarnedTV=findViewById(R.id.totalEarnReferPg);
        if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(getApplicationContext(), noInternetActivity.class));
        else{
            Dialog progressDialog = new Dialog(this);
            progressDialog.setContentView(R.layout.loading_panel);
            progressDialog.setCancelable(false);
            progressDialog.show();
            db.collection("userDetails").document(userDetails.getString("mobileNumber","NA"))
                    .collection("basicUserDetails").document("balances")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            totalEarnedTV.setText(String.valueOf(task.getResult().getDouble("referralTotalEarned")));
                            totalBalance= Objects.requireNonNull(task.getResult().getDouble("totalAddedBalance"));
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                            Toast.makeText(getApplicationContext(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        findViewById(R.id.inviteBtnReferPg).setOnClickListener(v -> {
            EditText mobileNumberET=findViewById(R.id.phoneReferringReferPg);
            String referredMobileNumber="+91"+mobileNumberET.getText().toString();
            if (isConnectionAvailable(getApplicationContext())) startActivity(new Intent(this, noInternetActivity.class));
            else if(totalBalance>=200) Toast.makeText(this, "You are not eligible for this\nRead Terms & Condition", Toast.LENGTH_SHORT).show();
            else if (referredMobileNumber.length()==13) {
                Dialog progressDialog = new Dialog(this);
                progressDialog.setContentView(R.layout.loading_panel);
                progressDialog.setCancelable(false);
                progressDialog.show();
                db.collection("userDetails")
                        .whereEqualTo(FieldPath.documentId(), referredMobileNumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
                                    db.collection("basicDetails").document("basicAttribute")
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task.isSuccessful()) {
                                                    Map<String, Object> dataList = new HashMap<>();
                                                    dataList.put("referredAt", getTime("dd MMM yyyy hh:mm a"));
                                                    dataList.put("referredBy", userDetails.getString("mobileNumber", "NA"));
                                                    dataList.put("referredTo", referredMobileNumber);
                                                    db.collection("referralDetails").document()
                                                            .set(dataList)
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(getApplicationContext(), "Data Stored successfully", Toast.LENGTH_SHORT).show();
                                                                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                                                                    .setType("text/plain")
                                                                                    .putExtra(Intent.EXTRA_TEXT, "Hii\nI am using this App and earning too much per day by prediction of color and number.\nIf you download this app from given link and enter my phone number in \"Referred By\" section you will get up to Rs 50 in your wallet.\nLink given Below:\n" + task1.getResult().getString("appUrl")),
                                                                            "Choose a platform"));
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
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Mobile Number is already Register", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }else Toast.makeText(this, "Enter Valid Mobile Number", Toast.LENGTH_SHORT).show();
        });
    }
}