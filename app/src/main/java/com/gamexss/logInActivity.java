package com.gamexss;

import static com.gamexss.MainActivity.getTime;
import static com.gamexss.MainActivity.isConnectionAvailable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class logInActivity extends AppCompatActivity {
    private SharedPreferences userDetails;
    private boolean isWaitingForOTP=false;
    private FirebaseAuth firebaseAuth;
    private int minuteOTP, secondOTP;
    private String mobileNumber;
    private EditText mobileNoET,OTP_ET;
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        firebaseAuth=FirebaseAuth.getInstance();
        userDetails=getSharedPreferences("userDetails",MODE_PRIVATE);
        mobileNoET=findViewById(R.id.mobileNumberLogInPg);
        OTP_ET=findViewById(R.id.otp);
        Button getOTP=findViewById(R.id.generateOTP);
        getOTP.setOnClickListener(v -> {
            if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
            else if(!isWaitingForOTP) {
                minuteOTP =2;
                secondOTP =0;
                mobileNoET.setEnabled(false);
                mobileNumber="+91"+mobileNoET.getText();
                mobileVerificationProcess(mobileNumber);
                isWaitingForOTP=true;
                final Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        if ((minuteOTP > 0 || secondOTP > 0)&&isWaitingForOTP) {
                            if (secondOTP == 0) {
                                minuteOTP--;
                                secondOTP = 59;
                            } else secondOTP--;
                            String str= minuteOTP +":"+ secondOTP;
                            getOTP.setText(str);
                            handler.postDelayed(this, 1000);
                        }
                        else {
                            isWaitingForOTP=false;
                            getOTP.setText(R.string.get_otp);
                        }
                    }
                };
                handler.post(r);
            }
            else Toast.makeText(this, "Wait for "+ minuteOTP +" minute "+ secondOTP +"second.", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.continueMobileVerification).setOnClickListener(v -> {
            verifyCode(String.valueOf(OTP_ET.getText()));
            isWaitingForOTP=false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Above fields are mandatory", Toast.LENGTH_SHORT).show();
    }
    private void mobileVerificationProcess(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // (optional) Activity for callback binding
                // If no activity is passed, reCAPTCHA verification can not be used.
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private String verificationId;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                final String code = credential.getSmsCode();
                if (code != null) {
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("error123",e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verificationId, token);
                logInActivity.this.verificationId = verificationId;
                Toast.makeText(logInActivity.this, "OTP sent on mobile", Toast.LENGTH_SHORT).show();
                Log.d("vid",verificationId);
            }
    };
    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }
    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                logging();
            }
            else if (task.getException() != null) Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
        });
    }
    private void logging(){
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.loading_panel);
        dialog.setCancelable(false);
        dialog.show();
        //to search a document in database...
        firebaseFirestore.collection("userDetails").document(mobileNumber).collection("basicUserDetails").document("profileAttributes")
            .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    SharedPreferences.Editor editor = userDetails.edit();
                    editor.putString("name", String.valueOf(document.get("name")))
                            .putString("mobileNumber",mobileNumber)
                            .putString("gender",String.valueOf(document.get("gender")))
                            .putString("dob",String.valueOf(document.get("dob")))
                            .putString("city",String.valueOf(document.get("city")))
                            .putBoolean("isLoggedIn",true)
                            .apply();
                    startActivity(new Intent(this,MainActivity.class));
                    finish();
                } else {
                    signup();
                    dialog.dismiss();
                }
            } else {
                dialog.dismiss();
                Log.d("data3", "get failed with ", task.getException());
                Toast.makeText(getApplicationContext(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private EditText name,city,referredBy;
    private RadioGroup genderRadioGroup;
    private RadioButton genderRadioButton;
    private TextView DOB;
    private CheckBox TC_CheckBox;
    private double totalBalance=0.0;
    void signup(){
        setContentView(R.layout.activity_sign_up);
        name=findViewById(R.id.fullNameSignPg);
        city=findViewById(R.id.citySignPg);
        genderRadioGroup=findViewById(R.id.genderRadioGroup);
        DOB=findViewById(R.id.DOB_SignPg);
        DOB.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> DOB.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),year, month, day);
            datePickerDialog.show();
        });
        referredBy=findViewById(R.id.referredByNoSignPg);
        findViewById(R.id.whyReferredBy).setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.game_rule_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.findViewById(R.id.cancelButtonGameRule).setOnClickListener(v1 -> dialog.dismiss());
            TextView desc = dialog.findViewById(R.id.gameRule),
            heading=dialog.findViewById(R.id.headingGameRule);
            String str="Why \"Referred By\" ?";
            heading.setText(str);
            String string = "This is used to confirm referrals from both sides.\nSo here enter the number of the person who referred you to this application.";
            desc.setText(string);
            dialog.show();
        });
        TC_CheckBox=findViewById(R.id.SignTC_CheckBox);
        findViewById(R.id.readSignTC_TV).setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.game_rule_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.findViewById(R.id.cancelButtonGameRule).setOnClickListener(v1 -> dialog.dismiss());
            TextView desc = dialog.findViewById(R.id.gameRule),
                    heading=dialog.findViewById(R.id.headingGameRule);
            String str="Terms & Condition";
            heading.setText(str);
            String string = "1.This game involves financial risk, please play at your own risk.\n2.Admins can monitor all profit records and change the referral bonus value.\3.In case of application failure, the money added to the wallet will be transferred to the account linked to your phone.\n4.In case of any illegal activity on your part your account will be blocked.";
            desc.setText(string);
            dialog.show();
        });
        findViewById(R.id.submitBtnSignPg).setOnClickListener(v -> {
            String referredByNumber="+91"+referredBy.getText().toString();
            if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
            else if (TextUtils.isEmpty(name.getText().toString())) Toast.makeText(this, "Please enter your Name.", Toast.LENGTH_SHORT).show();
            else if (genderRadioGroup.getCheckedRadioButtonId()==-1) Toast.makeText(this, "Please select your gender.", Toast.LENGTH_SHORT).show();
            else if (TextUtils.isEmpty(DOB.getText().toString())) Toast.makeText(this, "Please pick your Date of Birth.", Toast.LENGTH_SHORT).show();
            else if (TextUtils.isEmpty(city.getText().toString())) Toast.makeText(this, "Please enter your address.", Toast.LENGTH_SHORT).show();
            else if (!referredByNumber.equals("+91")&&referredByNumber.length() !=13) Toast.makeText(this, "Please enter Valid Phone number.", Toast.LENGTH_SHORT).show();
            else if (!TC_CheckBox.isChecked()) Toast.makeText(this, "Please accept terms & condition.", Toast.LENGTH_SHORT).show();
            else if(referredByNumber.length()==13){
                firebaseFirestore.collection("referralDetails")
                        .whereEqualTo("referredBy",referredByNumber)
                        .whereEqualTo("referredTo",mobileNumber)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> document = task.getResult().getDocuments();
                                if (document.isEmpty()) {
                                    Toast.makeText(this, "You are not referred by"+referredByNumber, Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    totalBalance=9.0;
                                    signingUp();
                                    firebaseFirestore.collection("referralDetails").document(document.get(0).getId()).delete();
                                    Map<String,Object> data=new HashMap<>();
                                    data.put("referralTotalEarned",FieldValue.increment(21));
                                    data.put("totalBalance",FieldValue.increment(21));
                                    firebaseFirestore.collection("userDetails").document(referredByNumber)
                                            .collection("basicUserDetails").document("balances").update(data);
                                }
                            }
                            else signingUp();
                        });
            }
            else {
                signingUp();
            }
        });
    }
    private void signingUp(){
        genderRadioButton = findViewById(genderRadioGroup.getCheckedRadioButtonId());
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", name.getText().toString());
        data2.put("phoneNumber", mobileNumber);
        data2.put("gender", genderRadioButton.getText().toString());
        data2.put("dob", DOB.getText().toString());
        data2.put("city", city.getText().toString());
        firebaseFirestore.collection("userDetails").document(mobileNumber).collection("basicUserDetails").document("profileAttributes").set(data2);
        Map<String, Object> balanceData = new HashMap<>();
        balanceData.put("profitBalance", 0.0);
        balanceData.put("referralTotalEarned", 0.0);
        balanceData.put("totalAddedBalance", 0.0);
        balanceData.put("totalBalance", totalBalance);
        firebaseFirestore.collection("userDetails").document(mobileNumber).collection("basicUserDetails").document("balances").set(balanceData);
        Map<String, Object> txnDataList = new HashMap<>();
        txnDataList.put("title", "Account Created");
        txnDataList.put("desc", "First Login");
        txnDataList.put("status", "Success");
        txnDataList.put("createdAt", getTime("dd MMM yyyy hh:mm a"));
        txnDataList.put("timeStamp", FieldValue.serverTimestamp());
        firebaseFirestore.collection("userDetails").document(mobileNumber).collection("txnHistory").document().set(txnDataList);
        userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = userDetails.edit();
        editor.putString("name", name.getText().toString())
                .putString("mobileNumber", mobileNumber)
                .putString("gender", genderRadioButton.getText().toString())
                .putString("dob", DOB.getText().toString())
                .putString("city", city.getText().toString())
                .putBoolean("isLoggedIn", true)
                .apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}