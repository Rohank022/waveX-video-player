package com.wavexvideoplayer.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wavexvideoplayer.R;
import com.wavexvideoplayer.adapter.FolderAdapter;
import com.wavexvideoplayer.videoModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private ArrayList<String> folderList = new ArrayList<>();
    private ArrayList<videoModel> videoList = new ArrayList<>();
    FolderAdapter folderAdapter;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView =findViewById(R.id.folder_recyclerview);
        videoList =fetchAllVideos(this);
        if (folderList != null && folderList.size() > 0 && videoList != null) {
            Log.d("MainActivity", "Context: " + this.toString());
            Log.d("MainActivity", "Folder List Size: " + folderList.size());
            Log.d("MainActivity", "Video List Size: " + videoList.size());
            folderAdapter = new FolderAdapter(folderList, videoList, this);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        } else {
            Toast.makeText(this, "Can't find any videos folder", Toast.LENGTH_SHORT).show();
        }


    }

    private ArrayList<videoModel> fetchAllVideos(Context context){

        ArrayList<videoModel> videoModels = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";

        String[] projection = {

                MediaStore.Video.Media._ID,

                MediaStore.Video.Media.DATA,

                MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE,

                MediaStore.Video.Media.HEIGHT,

                MediaStore.Video.Media.DURATION,

                MediaStore.Video.Media.DISPLAY_NAME,

                MediaStore.Video.Media.RESOLUTION



        };


        Cursor cursor = context.getContentResolver().query(uri, projection,null, null, orderBy);

        if (cursor != null) {

            while (cursor.moveToNext()) {

                String id = cursor.getString(0);

                String path = cursor.getString(1);

                String title = cursor.getString(2);

                String size = cursor.getString(3);

                String resolution = cursor.getString(4);

                String duration = cursor.getString(5);

                String disName = cursor.getString(6);

                String width_height = cursor.getString(7);

                videoModel videoFiles = new videoModel(id, path, title, size, resolution, duration, disName, width_height);

                int slashFireIndex = path.lastIndexOf("/");
                String subString = path.substring(0, slashFireIndex);

                if (!folderList.contains(subString)) {
                    folderList.add(subString);
                }

                videoModels.add(videoFiles);

            }

            cursor.close();
        }

        return videoModels;



    }

    // Method to fetch video data and initialize the adapter
    private void refreshVideoData() {
        videoList = fetchAllVideos(this);
        folderList.clear();

        for (videoModel video : videoList) {
            String path = video.getPath();
            int slashFireIndex = path.lastIndexOf("/");
            String subString = path.substring(0, slashFireIndex);
            if (!folderList.contains(subString)) {
                folderList.add(subString);
            }
        }

        if (folderList.size() > 0 && videoList.size() > 0) {
            folderAdapter = new FolderAdapter(folderList, videoList, this);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        } else {
            Toast.makeText(this, "Can't find any videos folder", Toast.LENGTH_SHORT).show();
        }
    }


    protected void onResume() {
        super.onResume();
        // Refresh video data and update the adapter when the activity is resumed
        refreshVideoData();
    }
}