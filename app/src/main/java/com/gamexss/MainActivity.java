package com.gamexss;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView matchNo,timerSecondDig1,timerSecondDig2,timerMinute, totalBalanceTV;
    private final Map<String,String> investmentDetails=new HashMap<>();
    private boolean investedOnNumber=false,investedOnColor=false;
    private TextView number1,number2,number3,number4,number5,number6,number7,number8,number9,number10;
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    private SharedPreferences userDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userDetails= getSharedPreferences("userDetails", MODE_PRIVATE);
        totalBalanceTV =findViewById(R.id.totalBalMainPg);
        db.collection("userDetails").document(userDetails.getString("mobileNumber","NA"))
                .collection("basicUserDetails").document("balances")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) totalBalanceTV.setText(new DecimalFormat("#.0").format(task.getResult().getDouble("totalBalance")));
                });
        matchNo = findViewById(R.id.matchNo);
        timerMinute = findViewById(R.id.timerMinuteDig2);
        timerSecondDig1 = findViewById(R.id.timerSecondDig1);
        timerSecondDig2= findViewById(R.id.timerSecondDig2);
        findViewById(R.id.walletBtnHome).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), walletActivity.class)));
        findViewById(R.id.shareBtnHome).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), referEarnActivity.class)));
        findViewById(R.id.addBalanceBtnHome).setOnClickListener(v -> {});
        findViewById(R.id.profileBtnHome).setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), profileActivity.class)));
        colorBtnClickListener(findViewById(R.id.redBtn));
        colorBtnClickListener(findViewById(R.id.greenBtn));
        colorBtnClickListener(findViewById(R.id.violetBtn));
        number1 = findViewById(R.id.number1);
        number2 = findViewById(R.id.number2);
        number3 = findViewById(R.id.number3);
        number4 = findViewById(R.id.number4);
        number5 = findViewById(R.id.number5);
        number6 = findViewById(R.id.number6);
        number7 = findViewById(R.id.number7);
        number8 = findViewById(R.id.number8);
        number9 = findViewById(R.id.number9);
        number10 = findViewById(R.id.number10);
        setColorOnNumbers();
        numberTextViewClickListener(number1);
        numberTextViewClickListener(number2);
        numberTextViewClickListener(number3);
        numberTextViewClickListener(number4);
        numberTextViewClickListener(number5);
        numberTextViewClickListener(number6);
        numberTextViewClickListener(number7);
        numberTextViewClickListener(number8);
        numberTextViewClickListener(number9);
        numberTextViewClickListener(number10);
        db.collection("earnRecList")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<QueryDocumentSnapshot> earnRecordArray=new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) earnRecordArray.add(document);
                        RecyclerView recyclerView = findViewById(R.id.earnRecordRecycler);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(new earnRecordAdapter(getApplicationContext(),earnRecordArray));
                    } else {
                        Log.d("database", "Error getting documents: ", task.getException());
                        Toast.makeText(getApplicationContext(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
        findViewById(R.id.readRule).setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.game_rule_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.findViewById(R.id.cancelButtonGameRule).setOnClickListener(v1 -> dialog.dismiss());
            TextView gameRuleTV = dialog.findViewById(R.id.gameRule),
                    heading=dialog.findViewById(R.id.headingGameRule);
            heading.setText(R.string.rule_of_the_game);
            String string = "If you invest 100 to trade on \"GREEN\", if the generated number's button color is green then you will get 190% of 100.\n OR \nIf you invest 100 to trade on number 4, if the generated number is 4 then you will get 380% of 100.";
            gameRuleTV.setText(string);
            dialog.show();
        });
    }
    void colorBtnClickListener(Button button){
        button.setOnClickListener(v -> {
            if (!investedOnNumber&&!investedOnColor) {
                colorInvestDialog(button.getText().toString());
            }
        });
    }
    void numberTextViewClickListener(TextView textView){
        textView.setOnClickListener(v -> {
            if (!investedOnNumber&&!investedOnColor) {
                numberInvestDialog(textView.getText().toString());
            }
        });
    }
    void setColorOnNumbers(){
        TextView[] textViews={number1,number2,number3,number4,number5,number6,number7,number8,number9,number10};
        for (TextView currentTextview:textViews) currentTextview.setBackgroundColor(colorToColorCode(numberToColor(randomNumberGenerator(2))));
    }
    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo == null || !netInfo.isConnectedOrConnecting() || !netInfo.isAvailable();
        }
        return true;
    }
    static String getTime(String format){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
            LocalDateTime now = LocalDateTime.now();
            return dtf.format(now);
        }
        return "#";
    }
    static int randomNumberGenerator(int limit){
        return new Random().nextInt(limit+1);
    }
    String numberToColor(int number){
        if(number==0) return "Red";
        else if (number==1) return "Green";
        else return "Violet";
    }
    String colorCodeToColor(int colorCode){
        if (colorCode==-1437680)return "Red";
        else if (colorCode==-9219409)return "Violet";
        else if (colorCode==-15233782)return "Green";
        else return "";
    }
    static int colorToColorCode(String color){
        if (Objects.equals(color, "Red"))return -1437680;
        else if (Objects.equals(color, "Violet"))return -9219409;
        else if (Objects.equals(color, "Green")) return -15233782;
        else return 0;
    }
    private int minute=3,secDig1=0,secDig2=0;
    private void gameProcess(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) matchNo.setText(getTime("yyMMddHHmmssMS"));
        else {
            Date date = new Date();
            matchNo.setText(String.valueOf(date.getTime()));
        }
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                if (minute > 0 || secDig1 > 0 || secDig2 > 0) {
                    if (secDig1 == 0 && secDig2 == 0) {
                        minute--;
                        secDig1 = 5;
                        secDig2=9;
                    } else if (secDig2 == 0){
                        secDig1--;
                        secDig2=9;
                    }else secDig2--;
                    timerSecondDig2.setText(String.valueOf(secDig2));
                    timerSecondDig1.setText(String.valueOf(secDig1));
                    timerMinute.setText(String.valueOf(minute));
                    handler.postDelayed(this, 1000);
                }
                else {
                    int randomNumber=randomNumberGenerator(9)+1;
                    startActivity(new Intent(getApplicationContext(), resultActivity.class)
                            .putExtra("investedAmount", investmentDetails.get("investedAmount"))
                            .putExtra("matchNo", matchNo.getText().toString())
                            .putExtra("investedOnNumber",investedOnNumber)
                            .putExtra("investedOnColor",investedOnColor)
                            .putExtra("createdAt", investmentDetails.get("createdAt"))
                            .putExtra("generatedNumber",randomNumber)
                            .putExtra("generatedColor",getColorOfNumber(randomNumber))
                            .putExtra("investedOn",investmentDetails.get("investedOn")));
                    investedOnColor=false;
                    investedOnNumber=false;
                    investmentDetails.clear();
                    recreate();
                }
            }
        };
        handler.post(r);
    }
    String getColorOfNumber(int number){
        ColorDrawable colorDrawable;
        switch (number){
            case 1 : colorDrawable= (ColorDrawable) number1.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 2 : colorDrawable= (ColorDrawable) number2.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 3 : colorDrawable= (ColorDrawable) number3.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 4 : colorDrawable= (ColorDrawable) number4.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 5 : colorDrawable= (ColorDrawable) number5.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 6 : colorDrawable= (ColorDrawable) number6.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 7 : colorDrawable= (ColorDrawable) number7.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 8 : colorDrawable= (ColorDrawable) number8.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 9 : colorDrawable= (ColorDrawable) number9.getBackground();return colorCodeToColor(colorDrawable.getColor());
            case 10 : colorDrawable= (ColorDrawable) number10.getBackground();return colorCodeToColor(colorDrawable.getColor());
            default:return "";
        }
    }
    private void colorInvestDialog(String colorSelect){
        Dialog colorInvestDialog=new Dialog(this);
        colorInvestDialog.setContentView(R.layout.invest_dialog);
        colorInvestDialog.setCanceledOnTouchOutside(false);
        EditText investingAmount=colorInvestDialog.findViewById(R.id.investingAmount);
        Spinner colorDropdown = colorInvestDialog.findViewById(R.id.colorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Red", "Green", "Violet"});
        colorDropdown.setAdapter(adapter);
        colorDropdown.setSelection(adapter.getPosition(colorSelect));
        colorInvestDialog.findViewById(R.id.investBtn).setOnClickListener(v -> {
            String amount=investingAmount.getText().toString(),color=colorDropdown.getSelectedItem().toString();
            double investedAmount=Double.parseDouble(amount);
            if(amount.equals("")) Toast.makeText(getApplicationContext(), "Enter Amount", Toast.LENGTH_SHORT).show();
            else if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
            else if (investedAmount>=10 && investedAmount<=500){
                Dialog progressDialog=new Dialog(this);
                progressDialog.setContentView(R.layout.loading_panel);
                progressDialog.setCancelable(false);
                db.collection("userDetails").document(userDetails.getString("mobileNumber","NA"))
                        .collection("basicUserDetails").document("balances")
                        .get()
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                double totalBalance=Objects.requireNonNull(task.getResult().getDouble("totalBalance"));
                                totalBalanceTV.setText(String.valueOf(totalBalance));
                                if(investedAmount<=totalBalance) {
                                    Toast.makeText(getApplicationContext(), "Invested on " + color + " Color", Toast.LENGTH_SHORT).show();
                                    investmentDetails.put("investedAmount", amount);
                                    investmentDetails.put("investedOn", color);
                                    investmentDetails.put("createdAt", getTime("dd MMM yyyy hh:mm a"));
                                    investedOnColor = true;
                                    colorInvestDialog.dismiss();
                                    setColorOnNumbers();
                                    gameProcess();
                                }
                                else Toast.makeText(getApplicationContext(), "Insufficient Balance!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else Toast.makeText(getApplicationContext(), "Enter amount between 10 to 500", Toast.LENGTH_SHORT).show();
        });
        colorInvestDialog.show();
    }
    private void numberInvestDialog(String numberSelected){
        Dialog numberInvestDialog=new Dialog(this);
        numberInvestDialog.setContentView(R.layout.invest_dialog);
        numberInvestDialog.setCanceledOnTouchOutside(false);
        EditText investingAmount=numberInvestDialog.findViewById(R.id.investingAmount);
        Spinner numberDropdown = numberInvestDialog.findViewById(R.id.colorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,new String[]{"1","2","3","4","5","6","7","8","9","10"});
        numberDropdown.setAdapter(adapter);
        numberDropdown.setSelection(adapter.getPosition(numberSelected));
        numberInvestDialog.findViewById(R.id.investBtn).setOnClickListener(v -> {
                    String amount = investingAmount.getText().toString(), number = numberDropdown.getSelectedItem().toString();
                    double investedAmount = Double.parseDouble(amount);
                    if (amount.equals("")) Toast.makeText(getApplicationContext(), "Enter Amount", Toast.LENGTH_SHORT).show();
                    else if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
                    else if (investedAmount >= 10 && investedAmount <= 500) {
                        Dialog progressDialog=new Dialog(getApplicationContext());
                        progressDialog.setContentView(R.layout.loading_panel);
                        progressDialog.setCancelable(false);
                        db.collection("userDetails").document(userDetails.getString("mobileNumber","NA"))
                                .collection("basicUserDetails").document("balances")
                                .get()
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        double totalBalance = Objects.requireNonNull(task.getResult().getDouble("totalBalance"));
                                        totalBalanceTV.setText(String.valueOf(totalBalance));
                                        if (investedAmount <= totalBalance) {
                                            Toast.makeText(getApplicationContext(), "Invested on " + number, Toast.LENGTH_SHORT).show();
                                            investmentDetails.put("investedAmount", amount);
                                            investmentDetails.put("investedOn", number);
                                            investmentDetails.put("createdAt", getTime("dd MMM yyyy hh:mm a"));
                                            investedOnNumber = true;
                                            numberInvestDialog.dismiss();
                                            setColorOnNumbers();
                                            gameProcess();
                                        } else Toast.makeText(getApplicationContext(), "Insufficient Balance!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else Toast.makeText(getApplicationContext(), "Enter amount between 10 to 500", Toast.LENGTH_SHORT).show();
        });
        numberInvestDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isConnectionAvailable(this)) startActivity(new Intent(this, noInternetActivity.class));
    }
}