package top.scraft.picman2.activity.adapter;

import android.content.Intent;

import androidx.annotation.NonNull;

import top.scraft.picman2.activity.ArrangeActivity;
import top.scraft.picman2.activity.PictureEditorActivity;
import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;

public class ArrangeAdapter extends AbstractGalleryAdapter {

    private final ArrangeActivity arrangeActivity;
    
    public ArrangeAdapter(ArrangeActivity arrangeActivity) {
        super(arrangeActivity);
        this.arrangeActivity = arrangeActivity;
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.getView().setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(arrangeActivity, PictureEditorActivity.class);
            intent.putExtra("picture_uri", pictureUriList.get(position).toString());
            arrangeActivity.startActivity(intent);
        });
    }

}
