package com.gamexss;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class txnHisRecyclerAdapter extends RecyclerView.Adapter<txnHisRecyclerAdapter.viewHolder> {
    private final Context context;
    private final ArrayList<QueryDocumentSnapshot> txnHisArray;
    txnHisRecyclerAdapter(Context context,ArrayList<QueryDocumentSnapshot> txnHisArray) {
        this.context = context;
        this.txnHisArray=txnHisArray;
    }

    @NonNull
    @Override
    public txnHisRecyclerAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.transaction_history_row, viewGroup, false);
        return new txnHisRecyclerAdapter.viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull txnHisRecyclerAdapter.viewHolder holder, int i) {
        QueryDocumentSnapshot docs = txnHisArray.get(holder.getAdapterPosition());
        holder.title.setText(String.valueOf(docs.get("title")));
        if (String.valueOf(docs.get("title")).equals("Debit from Wallet")){
            holder.amount.setText(String.valueOf("-"+docs.get("amount")));
            holder.amount.setTextColor(Color.RED);
        }else if(String.valueOf(docs.get("title")).equals("Credit in Wallet")){
            holder.amount.setText(String.valueOf("+"+docs.get("amount")));
            holder.amount.setTextColor(Color.GREEN);
        }else holder.amount.setText("");
        holder.desc.setText(String.valueOf(docs.get("desc")));
        holder.status.setText(String.valueOf(docs.get("status")));
        if(String.valueOf(docs.get("status")).equals("Success")) holder.status.setTextColor(Color.GREEN);
        else if(String.valueOf(docs.get("status")).equals("Failed")) holder.status.setTextColor(Color.RED);
        else holder.status.setTextColor(Color.parseColor("#735F00"));
        holder.createdAt.setText(String.valueOf(docs.get("createdAt")));
    }

    @Override
    public int getItemCount() {
        return txnHisArray.size();
    }
    public static class viewHolder extends RecyclerView.ViewHolder {
        private final TextView title, amount, desc, status, createdAt;
        public viewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.titleTxnHis);
            amount= v.findViewById(R.id.amountTxnHis);
            desc= v.findViewById(R.id.descTxnHis);
            status= v.findViewById(R.id.statusTxnHis);
            createdAt = v.findViewById(R.id.createdAtTxnHis);
        }
    }
}