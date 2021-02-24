package top.scraft.picman2.activity.adapter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;

@RequiredArgsConstructor
public abstract class AbstractGalleryAdapter extends RecyclerView.Adapter<ImageViewHolder> {
    
    private final Activity activity;

    @Getter
    protected final ArrayList<Uri> pictureUriList = new ArrayList<>();

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = pictureUriList.get(position);
        try {
            holder.setImage(BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(activity, "加载预览图失败 " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return pictureUriList.size();
    }
}
