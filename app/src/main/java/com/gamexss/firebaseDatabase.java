package com.gamexss;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class firebaseDatabase {
    private final FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    Map<String,Object> document=new HashMap<>();
    void addDataInCollection(Context context, String collectionName, String documentName, Map<String,String> dataList) {
        CollectionReference collection = firebaseFirestore.collection(collectionName);
        collection.document(documentName).set(dataList)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Toast.makeText(context, "Data Stored successfully", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(context, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                });
    }
    void addDataInNestedCollection(Context context,String IstCollectionName,String IstDocumentName,String IIndCollectionName,Map<String,String> dataList) {
        CollectionReference collection = firebaseFirestore.collection(IstCollectionName).document(IstDocumentName).collection(IIndCollectionName);
        collection.document().set(dataList)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Toast.makeText(context, "Data Stored successfully", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(context, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                });
    }
    String string;
     void getDataFromDatabase(Context context, String collectionName, String documentName){
        DocumentReference docRef = firebaseFirestore.collection(collectionName).document(documentName);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    document=documentSnapshot.getData();
                } else {
                    Log.d("firebase-1", "No such document");
                }
            } else {
                Log.d("firebase-1", "get failed with ", task.getException());
            }
        });
    }
    ArrayList<QueryDocumentSnapshot> documentArray=new ArrayList<>();
     int arraySize=-1;
    void searchDocument(Query query) {

    }
    void getAllDocuments(String collectionName) {
        Task<QuerySnapshot> documentReference = firebaseFirestore.collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            documentArray.add(queryDocumentSnapshot);
                        }
                    } else {
                        Log.d("firebase3", "Error getting documents: ", task.getException());
                    }
                });
    }

}
