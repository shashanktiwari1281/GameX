package com.gamexss;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class earnRecordAdapter extends RecyclerView.Adapter<earnRecordAdapter.viewHolder>{
    private final Context context;
    private final ArrayList<QueryDocumentSnapshot> earnRecArray;
    public earnRecordAdapter(Context context, ArrayList<QueryDocumentSnapshot> earnRecArray) {
        this.context = context;
        this.earnRecArray=earnRecArray;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.earn_list_row, viewGroup, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int i) {
        QueryDocumentSnapshot docs = earnRecArray.get(holder.getAdapterPosition());
        String matchNoStr="#"+docs.getId();
        holder.matchNumber.setText(matchNoStr);
        holder.genNumber.setText(String.valueOf(docs.get("generatedNo")));
        holder.genColor.setColorFilter(MainActivity.colorToColorCode(String.valueOf(docs.get("color"))));
        holder.wonAmount.setText(String.valueOf(docs.get("wonAmount")));
    }

    @Override
    public int getItemCount() {return earnRecArray.size();}
    public static class viewHolder extends RecyclerView.ViewHolder {
        ImageView genColor;
        TextView matchNumber, genNumber, wonAmount;
        public viewHolder(View v) {
            super(v);
            matchNumber = v.findViewById(R.id.matchNoEarnRec);
            genNumber = v.findViewById(R.id.generatedNoEarnRec);
            genColor = v.findViewById(R.id.colorEarnRec);
            wonAmount = v.findViewById(R.id.wonAmountEarnRec);
        }
    }
}
class earnListModel {
    String matchNo,color,generatedNo,wonAmount;
    earnListModel(String matchNo,String generatedNo,String color,String wonAmount){
        this.matchNo=matchNo;
        this.generatedNo=generatedNo;
        this.color=color;
        this.wonAmount=wonAmount;
    }
}
