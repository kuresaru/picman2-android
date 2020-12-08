package top.scraft.picman2.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import top.scraft.picman2.R;
import top.scraft.picman2.storage.dao.PictureLibrary;

import java.util.ArrayList;

public class PiclibInfoAdapter extends BaseAdapter {

    private final Context context;
    private final PictureLibrary library;
    private final ArrayList<String> content = new ArrayList<>();

    public PiclibInfoAdapter(Context context, PictureLibrary library) {
        this.context = context;
        this.library = library;
        content.add("ID=" + (library.getOffline() ? "N/A" : library.getLid()));
        content.add("图库名=" + library.getName());
        content.add("所有者=" + (library.getOffline() ? "N/A" : library.getOwner()));
        content.add("图片数=" + 0);
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public String getItem(int i) {
        return content.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_piclib_info, null);
        }
        if (convertView != null) {
            String keyValue = getItem(i);
            TextView key = convertView.findViewById(R.id.piclib_info_key);
            TextView value = convertView.findViewById(R.id.piclib_info_value);
            key.setText(keyValue.substring(0, keyValue.indexOf("=")));
            value.setText(keyValue.substring(keyValue.indexOf("=") + 1));
        }
        return convertView;
    }

}
