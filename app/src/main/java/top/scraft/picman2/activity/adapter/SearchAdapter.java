package top.scraft.picman2.activity.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.PictureStorageController;
import top.scraft.picman2.storage.dao.Picture;

public class SearchAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private final List<Picture> pictures;
    private final PictureStorageController pictureStorageController;

    public SearchAdapter(Context context, List<Picture> pictures) {
        this.pictures = pictures;
        this.pictureStorageController = PicmanStorage.getInstance(context).getPictureStorage();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.getView().setImageURI(Uri.fromFile(pictureStorageController.getPicturePath(pictures.get(position).getPid())));
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

}
