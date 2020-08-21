package top.scraft.picman2.activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import top.scraft.picman2.R;

public class ArrangeViewHolder extends RecyclerView.ViewHolder {

    private final int size;

    public ArrangeViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_arrange_grid_picture, parent, false));
        size = parent.getWidth() / 4;
        itemView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
    }

    public ImageView getView() {
        return (ImageView) itemView;
    }

}
