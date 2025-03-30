package com.gamexss;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class launcherActivity extends AppCompatActivity {
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences userDetails= getSharedPreferences("userDetails", MODE_PRIVATE);
        if(MainActivity.isConnectionAvailable(getApplicationContext())) startActivity(new Intent(getApplicationContext(),noInternetActivity.class));
        else{
            db.collection("basicDetails").document("basicAttribute")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!Objects.equals(getString(R.string.app_name), task.getResult().getString("appVersion"))){
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Update Available!");
                                builder.setMessage("Download and Install the new version to continue service...");
                                builder.setPositiveButton("Download", (dialog, which) -> {
                                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(task.getResult().getString("appUrl"))));
                                    dialog.dismiss();
                                });
                                builder.setNegativeButton("Ignore", (dialog, which) -> {
                                    finish();
                                    dialog.dismiss();
                                });
                                builder.create().show();
                            }
                            else if (!userDetails.getBoolean("isLoggedIn", false)){startActivity(new Intent(this,logInActivity.class));finish();}
                            else {
                                new Handler().postDelayed(() -> {
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }, 2000);
                            }
                        } else {
                            Log.d("database", "Error getting documents: ", task.getException());
                            Toast.makeText(getApplicationContext(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}