package com.wavexvideoplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wavexvideoplayer.R;
import com.wavexvideoplayer.activity.videoFolder;
import com.wavexvideoplayer.videoModel;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {

    private ArrayList<String> folderName;
    private ArrayList<videoModel> videoModels;
    private Context context;


    public FolderAdapter(ArrayList<String> folderline, ArrayList<videoModel> videoModels, Context context) {
        this.folderName = folderline;
        this.videoModels = videoModels;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.folder_view, parent , false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.MyViewHolder holder,int position) {

        int index= folderName.get(position).lastIndexOf("/");
        String folderNames = folderName.get(position).substring(index+1);

        holder.name.setText(folderNames);
        holder.countVideos.setText(String.valueOf(countVideos(folderName.get(position))));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.item_click_animation);
                holder.itemView.startAnimation(scaleAnimation);


                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(context, videoFolder.class);
                    intent.putExtra("folderName", folderName.get(clickedPosition));
                    context.startActivity(intent);
                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return folderName.size();
    }





    public class MyViewHolder extends  RecyclerView.ViewHolder {
        TextView name, countVideos;
        public MyViewHolder (@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.folderName);
            countVideos = itemView.findViewById(R.id.videosCount);
        }
    }

    int countVideos(String folderPath){
        int count = 0;

        for (videoModel model : videoModels){
            String videoFolderPath = model.getPath().substring(0, model.getPath().lastIndexOf("/"));
            if (videoFolderPath.equals(folderPath)){
                count++;
            }
        }
        return count;
    }

    public void setVideoModels(ArrayList<videoModel> videoModels) {
        this.videoModels = videoModels;
        notifyDataSetChanged(); // Notify the adapter about the data change
    }

}

