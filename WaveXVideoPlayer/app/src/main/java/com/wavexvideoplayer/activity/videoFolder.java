package com.wavexvideoplayer.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wavexvideoplayer.R;
import com.wavexvideoplayer.adapter.videosAdapter;
import com.wavexvideoplayer.videoModel;

import java.util.ArrayList;
import java.util.Locale;

public class videoFolder extends AppCompatActivity {


    private RecyclerView recyclerView;
    private String name;
    private ArrayList<videoModel> videoModelArrayList= new ArrayList<>();
    private videosAdapter videoAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_folder);

        name= getIntent().getStringExtra("folderName");
        recyclerView = findViewById(R.id.video_recyclerview);

        loadVideos();
    }

    private void loadVideos() {

        videoModelArrayList =  getAllVideoFromFolder(this, name);
        if (name!=null && videoModelArrayList.size()>0){

            videoAdapter= new videosAdapter(videoModelArrayList, this);

            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setNestedScrollingEnabled(false);

            recyclerView.setAdapter(videoAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
        }
        else {
            Toast.makeText(this, "can't find any videos", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<videoModel> getAllVideoFromFolder(Context context, String name) {
        ArrayList<videoModel> list = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String orderBy= MediaStore.Video.Media.DATE_ADDED + " DESC";

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media. BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media. RESOLUTION
         };

        String selection = MediaStore.Video.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + name + "%"};

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy);

        if (cursor!=null){

            while(cursor.moveToNext()){
                String id = cursor.getString(0);
                String path =cursor.getString(  1);
                String title =cursor.getString(  2);
                int size= cursor.getInt( 3);
                String resolution =cursor.getString(  4);
                int duration= cursor.getInt( 5);
                String disName =cursor.getString( 6);
                String bucket_display_name = cursor.getString( 7);
                String width_height= cursor.getString(8);


             //this method convert 1204 in 1MB
                String human_can_read=null;

               if (size<1024 )
               {
                   human_can_read =  String.format(context.getString(R.string.size_in_b),(double) size);
               } else if (size < Math.pow(1024,2)) {
                   human_can_read =  String.format(context.getString(R.string.size_in_kb),(double) (size/1024));
               } else if (size< Math.pow(1024,  3)) {
                   human_can_read =  String.format(context.getString(R.string.size_in_mb),(double) (size/Math.pow(1024,2)));
               }
               else {
                   human_can_read =  String.format(context.getString(R.string.size_in_gb),(double) (size/Math.pow(1024,3)));
               }

               // method to convert random duration into 1:00:00

                String duration_formatted;
               int sec= (duration/1000) % 60;
               int min= (duration/(1000*60)) % 60;
               int hr= (duration/(1000*60*60)) % 60;

               if (hr==0){
                   duration_formatted=String.valueOf(min).concat(":".concat(String.format(Locale.UK, "%02d", sec)));
               }
               else {
                   duration_formatted= String.valueOf(hr).concat(":".concat(String.format(Locale.UK, "%02d",min)
                           .concat(String.format(Locale.UK, "%02d",sec))));
               }

               videoModel files = new videoModel(id, path, title,
                       human_can_read, resolution, duration_formatted, disName, width_height);
               if (name.endsWith(bucket_display_name))
                   list.add(files);


               }cursor.close();
            }
                return list;
    }


}