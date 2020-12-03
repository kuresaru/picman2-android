package top.scraft.picman2.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import lombok.RequiredArgsConstructor;
import top.scraft.picman2.R;
import top.scraft.picman2.storage.dao.PictureLibrary;

import java.util.List;

@RequiredArgsConstructor
public class PiclibManagerAdapter extends BaseAdapter {

    private final Context context;
    private final List<PictureLibrary> infoList;

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public PictureLibrary getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return infoList.get(position).getAppInternalLid();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_piclib, null);
        PictureLibrary library = getItem(position);
        TextView title = view.findViewById(R.id.piclib_title);
        title.setText(library.getName());
        return view;
    }

}
