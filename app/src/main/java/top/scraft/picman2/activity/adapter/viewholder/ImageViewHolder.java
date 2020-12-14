package top.scraft.picman2.activity.adapter.viewholder;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import top.scraft.picman2.R;

public class ImageViewHolder extends RecyclerView.ViewHolder {

    private static Animation animation;

    private final int size;

    public ImageViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_picture, parent, false));
        size = parent.getWidth() / 4;
        itemView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(parent.getContext(), R.anim.icon_rotate);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(Animation.INFINITE);
        }
        getView().setBackgroundResource(R.drawable.ic_baseline_sync_24);
    }

    public ImageView getView() {
        return (ImageView) itemView;
    }

    public void setImage(@Nullable File file) {
        if (file == null) {
            getView().startAnimation(animation);
        } else {
            getView().clearAnimation();
            getView().setAnimation(null);
            getView().setImageURI(Uri.fromFile(file));
        }
    }

}
