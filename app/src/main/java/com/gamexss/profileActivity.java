package com.gamexss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Objects;

public class profileActivity extends AppCompatActivity {
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.backBtnProfilePg).setOnClickListener(v -> finish());
        SharedPreferences userDetails=getSharedPreferences("userDetails",MODE_PRIVATE);
        TextView name=findViewById(R.id.userNameProfilePg),
                mobileNumber=findViewById(R.id.mobileNoProfilePg),
                totalBalanceTV=findViewById(R.id.totalBalProfilePg),
                gender=findViewById(R.id.genderProfilePg),
                dob=findViewById(R.id.dobProfilePg),
                city=findViewById(R.id.cityProfilePg);
        name.setText(userDetails.getString("name","NA"));
        mobileNumber.setText(userDetails.getString("mobileNumber","NA"));
        db.collection("userDetails").document(userDetails.getString("mobileNumber","NA"))
                .collection("basicUserDetails").document("balances")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double totalBalance=Objects.requireNonNull(task.getResult().getDouble("totalBalance"));
                        totalBalanceTV.setText(new DecimalFormat("#.0").format(totalBalance));
                    } else {
                        Log.d("firebase3", "Error getting documents: ", task.getException());
                    }
                });
        gender.setText(userDetails.getString("gender","NA"));
        dob.setText(userDetails.getString("dob","00/00/0000"));
        city.setText(userDetails.getString("city","NA"));
    }
}