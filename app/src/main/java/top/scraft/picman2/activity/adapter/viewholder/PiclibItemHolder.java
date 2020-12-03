package top.scraft.picman2.activity.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import top.scraft.picman2.R;

public class PiclibItemHolder extends RecyclerView.ViewHolder {

    public PiclibItemHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_piclib, parent, false));
    }

    public ImageView getView() {
        return (ImageView) itemView;
    }

}
