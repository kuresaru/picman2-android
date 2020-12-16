package top.scraft.picman2.activity.adapter;

import android.content.Intent;
import androidx.annotation.NonNull;
import lombok.RequiredArgsConstructor;
import top.scraft.picman2.activity.ArrangeActivity;
import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;
import top.scraft.picman2.activity.PictureEditorActivity;

@RequiredArgsConstructor
public class ArrangeAdapter extends AbstractGalleryAdapter {

    private final ArrangeActivity arrangeActivity;

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.getView().setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(arrangeActivity, PictureEditorActivity.class);
            intent.putExtra("file", picturePathList.get(position));
            arrangeActivity.startActivity(intent);
        });
    }

}
