package com.gamexss;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class transactionHistory extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.loading_panel);
        dialog.setCancelable(false);
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        SharedPreferences userDetails=getSharedPreferences("userDetails",MODE_PRIVATE);
        db.collection("userDetails").document(userDetails.getString("mobileNumber","Blank")).collection("txnHistory")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<QueryDocumentSnapshot> txnHisArray=new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) txnHisArray.add(document);
                        RecyclerView txnHistoryRecycler = findViewById(R.id.recyclerTxnHistory);
                        txnHistoryRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        txnHisRecyclerAdapter adapter = new txnHisRecyclerAdapter(getApplicationContext(),txnHisArray);
                        dialog.dismiss();
                        txnHistoryRecycler.setAdapter(adapter);
                    } else {
                        Log.d("database", "Error getting documents: ", task.getException());
                        Toast.makeText(getApplicationContext(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}