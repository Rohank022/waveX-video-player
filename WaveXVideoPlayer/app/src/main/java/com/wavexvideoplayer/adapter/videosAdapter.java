package com.wavexvideoplayer.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.wavexvideoplayer.R;
import com.wavexvideoplayer.videoModel;

import java.io.File;
import java.util.ArrayList;
public class videosAdapter extends RecyclerView.Adapter<videosAdapter.MyHolder> {
    private ArrayList<videoModel> videoFolder =new ArrayList<>();
    private Context context;

    public videosAdapter(ArrayList<videoModel> videoFolder, Context context) {
        this.videoFolder = videoFolder;
        this.context = context;
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.files_view, parent , false);
        return new MyHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Glide.with(context).load(videoFolder.get(position).getPath()).into(holder.thumbnail);
        holder.title.setText(videoFolder.get(position).getTitle());
        holder.duration.setText(videoFolder.get(position).getDuration());
        holder.size.setText(videoFolder.get(position).getSize());
        holder.resolution.setText(videoFolder.get(position).getResolution());
        holder.menu.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context,R.style.BottomSheetDialogueTheme);
            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.file_menu,null);

            bottomSheetView.findViewById(R.id.menu_down).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
            } );
        bottomSheetView.findViewById(R.id.menu_share).setOnClickListener(v2 ->{
            bottomSheetDialog.dismiss();
            shareFile(position);

        });
        bottomSheetView.findViewById(R.id.menu_rename).setOnClickListener(v3->{
            bottomSheetDialog.dismiss();
            renameFiles(position, v);


        });
        bottomSheetView.findViewById(R.id.menu_delete).setOnClickListener(v4->{
            bottomSheetDialog.dismiss();
            deleteFiles(position, v);
        });
        bottomSheetView.findViewById(R.id.menu_properties).setOnClickListener(v5->{
            bottomSheetDialog.dismiss();
            showProperties(position);
        });
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });
    }
    private void shareFile(int p) {

        Uri uri= Uri.parse(videoFolder.get(p).getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri); context.startActivity(Intent.createChooser (intent, "share"));

    }
    private void deleteFiles(int p, View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("delete")
                .setMessage("Are you sure you want to delete '" + videoFolder.get(p).getTitle() + "'?")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //todo
                        // leave it as empty
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri contentUri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                Long.parseLong(videoFolder.get(p).getId()));
                        File file = new File(videoFolder.get(p).getPath());
                        boolean deleted = file.delete();
                        if (deleted){
                            context.getApplicationContext().getContentResolver()
                                    .delete(contentUri,
                                            null, null);
                            videoFolder.remove(p);
                            notifyItemRemoved(p);
                            notifyItemRangeChanged(p,videoFolder.size());
                            Snackbar.make(view,"File Deleted Success",
                                    Snackbar.LENGTH_SHORT).show();
                        }else {
                            Snackbar.make(view,"File Deleted Fail",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }
    private void renameFiles(int position, View view) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rename_layout);
        final EditText editText = dialog.findViewById(R.id.rename_edit_text);
        Button cancel = dialog.findViewById(R.id.cancel_rename_button);
        Button renameBtn = dialog.findViewById(R.id.rename_button);
        final File renameFile = new File(videoFolder.get(position).getPath());
        String nameText = renameFile.getName();
        nameText = nameText.substring(0, nameText.lastIndexOf("."));
        editText.setText(nameText);
        editText.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        cancel.setOnClickListener(v -> dialog.dismiss());

        renameBtn.setOnClickListener(v1 -> {
            String newName = editText.getText().toString();
            if (newName.isEmpty()) {
                Snackbar.make(view, "Please enter a valid name", Snackbar.LENGTH_SHORT).show();
            } else {
                String onlyPath = renameFile.getParentFile().getAbsolutePath();
                String ext = renameFile.getAbsolutePath().substring(renameFile.getAbsolutePath().lastIndexOf("."));
                String newPath = onlyPath + "/" + newName + ext;
                File newFile = new File(newPath);

                // Perform renaming in a background thread
                new Thread(() -> {
                    boolean renameSuccess = renameFile.renameTo(newFile);
                    if (renameSuccess) {
                        // Update media database
                        context.getApplicationContext().getContentResolver()
                                .delete(MediaStore.Files.getContentUri("external"),
                                        MediaStore.MediaColumns.DATA + "=?",
                                        new String[]{renameFile.getAbsolutePath()});
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(Uri.fromFile(newFile));
                        context.getApplicationContext().sendBroadcast(mediaScanIntent);

                        // Update the adapter data source
                        videoFolder.get(position).setPath(newFile.getAbsolutePath());
                        videoFolder.get(position).setTitle(newFile.getName());
                        videoFolder.get(position).setId(String.valueOf(newFile.lastModified()));

                        // Update UI on the main thread
                        ((Activity) context).runOnUiThread(() -> {
                            notifyItemChanged(position);
                            Snackbar.make(view, "Rename Success", Snackbar.LENGTH_SHORT).show();
                        });
                    } else {
                        ((Activity) context).runOnUiThread(() -> {
                            Snackbar.make(view, "Rename Failed", Snackbar.LENGTH_SHORT).show();
                        });
                    }
                    // Dismiss the dialog on the main thread
                    ((Activity) context).runOnUiThread(dialog::dismiss);
                }).start();
            }
        });

        dialog.show();
    }


    private void showProperties(int p){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.file_properties);

        String name = videoFolder.get(p).getTitle();
        String path = videoFolder.get(p).getPath();
        String size = videoFolder.get(p).getSize();
        String duration = videoFolder.get(p).getDuration();
        String resolution = videoFolder.get(p).getResolution();

        TextView tit = dialog.findViewById(R.id.pro_title);
        TextView st = dialog.findViewById(R.id.pro_storage);
        TextView siz = dialog.findViewById(R.id.pro_size);
        TextView dur = dialog.findViewById(R.id.pro_duration);
        TextView res = dialog.findViewById(R.id.pro_resolution);

        tit.setText(name);
        st.setText(path);
        siz.setText(size);
        dur.setText(duration);
        res.setText(resolution+"p");
        dialog.show();

    }
    @Override
    public int getItemCount() {
        return videoFolder.size();
    }
    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView thumbnail, menu;
        TextView title, size , duration, resolution;
        public MyHolder(@NonNull View itemView){
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.video_title);
            size = itemView.findViewById(R.id.video_size);
            duration = itemView.findViewById(R.id.video_duration);
            resolution = itemView.findViewById(R.id.video_quality);
            menu = itemView.findViewById(R.id.video_menu);
        }
    }
}



