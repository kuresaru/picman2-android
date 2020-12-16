package top.scraft.picman2.activity.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import lombok.Getter;
import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;

public abstract class AbstractGalleryAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    @Getter
    protected final ArrayList<String> picturePathList = new ArrayList<>();

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.setImage(new File(picturePathList.get(position)));
    }

    @Override
    public int getItemCount() {
        return picturePathList.size();
    }
}
