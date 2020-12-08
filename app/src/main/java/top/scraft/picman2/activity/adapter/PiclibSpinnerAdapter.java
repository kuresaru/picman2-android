package top.scraft.picman2.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import top.scraft.picman2.storage.dao.PictureLibrary;

public class PiclibSpinnerAdapter extends ArrayAdapter<PictureLibrary> {

    private final LayoutInflater inflater;
    private final int dropDownResource;

    public PiclibSpinnerAdapter(Context context, List<PictureLibrary> piclibs) {
        super(context, android.R.layout.simple_spinner_item, piclibs);
        inflater = LayoutInflater.from(context);
        dropDownResource = android.R.layout.simple_spinner_dropdown_item;
        setDropDownViewResource(dropDownResource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(inflater, position, convertView, parent, android.R.layout.simple_spinner_item);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(inflater, position, convertView, parent, dropDownResource);
    }

    private View createViewFromResource(LayoutInflater inflater, int position, View convertView, ViewGroup parent, int resource) {
        final TextView text;
        if (convertView == null) {
            text = (TextView) inflater.inflate(resource, parent, false);
        } else {
            text = (TextView) convertView;
        }

        final PictureLibrary item = getItem(position);
        text.setText(item.getName());

        return text;
    }

}
