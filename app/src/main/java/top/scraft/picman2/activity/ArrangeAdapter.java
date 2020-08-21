package top.scraft.picman2.activity;

import android.util.Log;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lombok.Getter;

import java.util.ArrayList;

public class ArrangeAdapter extends RecyclerView.Adapter<ArrangeViewHolder> {

    @Getter
    private ArrayList<String> picturePathList = new ArrayList<>();

    @NonNull
    @Override
    public ArrangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArrangeViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ArrangeViewHolder holder, int position) {
        Log.i("ArrangeActivity", "Test: Load Image " + picturePathList.get(position));
    }

    @Override
    public int getItemCount() {
        return picturePathList.size();
    }

}
