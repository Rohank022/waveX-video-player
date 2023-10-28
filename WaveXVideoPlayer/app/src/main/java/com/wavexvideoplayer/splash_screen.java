package com.wavexvideoplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.wavexvideoplayer.activity.MainActivity;

public class splash_screen extends AppCompatActivity {

    public static final int REQUEST_CODE_PERMISSION = 123;
    public static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 124;
    private boolean writePermissionRequested = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        ImageView image = findViewById(R.id.imageView);
        TextView text = findViewById(R.id.textView);

        // Load fade-in animation from the anim resource folder
        Animation popup;
        popup = AnimationUtils.loadAnimation(this, R.anim.pop_up);


        // Set the animation to the ImageView and TextView
        image.startAnimation(popup);

        // Delay the animation of the TextView by 300 to 400 milliseconds
        new Handler().postDelayed(() -> {
             // Set the text to be visible after the delay
            text.startAnimation(popup);
        }, 300);





        Thread td=new Thread(){
            public void run(){
                try {
                    sleep(2000);
                }
                catch (Exception  e){
                    e.printStackTrace();

                } finally {
                    checkPermissions();
                }
            }
        }; td.start();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {
            // Request WRITE_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Request MANAGE_EXTERNAL_STORAGE permission for devices running Android 11 and above
            requestManageExternalStoragePermission();
        } else {
            // Both permissions granted, proceed to the next activity
            proceedToNextActivity();
        }
    }

    private void requestManageExternalStoragePermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // WRITE_EXTERNAL_STORAGE permission granted, check for MANAGE_EXTERNAL_STORAGE permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    requestManageExternalStoragePermission();
                } else {
                    // MANAGE_EXTERNAL_STORAGE permission already granted, proceed to the next activity
                    proceedToNextActivity();
                }
            } else {
                // WRITE_EXTERNAL_STORAGE permission denied, show alert dialog
                showPermissionDeniedDialog("Media Access Permission Denied",
                        "We need your permission to access your media files in order to provide you with seamless video browsing experience. Please grant the necessary permission from the app settings.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            // Check if MANAGE_EXTERNAL_STORAGE permission was granted after user interaction with settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                proceedToNextActivity();
            } else {
                // MANAGE_EXTERNAL_STORAGE permission denied, show alert dialog
                showPermissionDeniedDialog("File Management Permission Denied",
                        "We need your permission to manage all files in order to provide you with a seamless video browsing experience. Please grant the necessary permission from the app settings.");
            }
        }
    }

    private void showPermissionDeniedDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Re-request the necessary permission
                checkPermissions();
            }
        });
        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the denial, e.g., show another message or guide the user to app settings
                Toast.makeText(splash_screen.this, "Permission Denied. Please grant the necessary permissions from the app settings.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.setCancelable(false); // Prevent dialog dismissal on outside touch or back press
        builder.show();
    }


    private void proceedToNextActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


}


