package top.scraft.picman2.activity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import lombok.RequiredArgsConstructor;
import top.scraft.picman2.R;
import top.scraft.picman2.activity.PicLibManagerActivity;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.dao.PictureLibrary;

import java.util.*;

@RequiredArgsConstructor
public class PiclibManagerAdapter extends BaseAdapter {

    private final PicLibManagerActivity context;
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_piclib, null);
        }
        if (convertView != null) {
            PictureLibrary library = getItem(position);
            TextView title = convertView.findViewById(R.id.piclib_title);
            String name = library.getName();
            if (library.getOffline()) {
                name = "[离线]" + name;
            }
            title.setText(name);
            convertView.setTag(library);
            convertView.setOnClickListener(this::showLibraryDialog);
        }
        return convertView;
    }

    private void showLibraryDialog(@NonNull View view) {
        Object o = view.getTag();
        if (o instanceof PictureLibrary) {
            PictureLibrary library = (PictureLibrary) o;
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_piclib_info, null);

            dialog.setTitle(library.getName());
            dialog.setView(dialogView);
            dialog.setNegativeButton(R.string.text_close, null);

            TabLayout tab = dialogView.findViewById(R.id.dialog_piclib_info_tabs);
            ViewPager pager = dialogView.findViewById(R.id.dialog_piclib_pager);
            tab.setupWithViewPager(pager, false);
            AlertDialog alertDialog = dialog.show();
            pager.setAdapter(new MyPagerAdapter(library, alertDialog));
        }
    }

    @RequiredArgsConstructor
    private class MyPagerAdapter extends PagerAdapter {
        private final String[] tabTitles = new String[]{"信息", "浏览", "共享", "操作"};
        private final PictureLibrary library;
        private final AlertDialog alertDialog;

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View ret = null;
            if (position == 0) {
                View v = LayoutInflater.from(context).inflate(R.layout.dialog_piclib_info_list, null);
                ListView listView = v.findViewById(R.id.dialog_piclib_info_list);
                listView.setAdapter(new PiclibInfoAdapter(context, library));
                ret = v;
            } else if (position == 1 || position == 2) {
                TextView textView = new TextView(context);
                textView.setText("TODO");
                ret = textView;
            } else if (position == 3) {
                ListView listView = new ListView(context);
                List<Map<String, Object>> options = new ArrayList<>();
                Map<String, Object> map;

                map = new HashMap<>();
                map.put("option", "转换为" + (library.getOffline() ? "在线" : "离线"));
                options.add(map);

                map = new HashMap<>();
                map.put("option", "删除");
                options.add(map);

                SimpleAdapter simpleAdapter = new SimpleAdapter(context, options, android.R.layout.simple_list_item_1,
                        new String[]{"option"}, new int[]{android.R.id.text1});
                listView.setAdapter(simpleAdapter);
                listView.setOnItemClickListener((parent, view1, position1, id) -> {
                    if ("删除".equals(options.get(position1).get("option"))) {
                        if (library.getPictures().size() > 0) {
                            Toast.makeText(context, "//TODO 暂不支持直接删除非空图库", Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder confirm = new AlertDialog.Builder(context);
                            confirm.setTitle("删除图库");
                            confirm.setMessage(String.format("确认删除图库[%s]?", library.getName()));
                            confirm.setNegativeButton(R.string.text_cancel, (d, w) -> alertDialog.show());
                            confirm.setPositiveButton(R.string.text_delete, (d, w) -> {
                                PicmanStorage.getInstance(context).getDaoSession().getPictureLibraryDao().deleteByKey(library.getAppInternalLid());
                                Iterator<PictureLibrary> itr = infoList.iterator();
                                while (itr.hasNext()) {
                                    PictureLibrary l = itr.next();
                                    if (l.getAppInternalLid().equals(library.getAppInternalLid())) {
                                        itr.remove();
                                    }
                                }
                                PiclibManagerAdapter.this.notifyDataSetChanged();
                            });
                            alertDialog.dismiss();
                            confirm.show();
                        }
                    }
                });
                ret = listView;
            }
            Objects.requireNonNull(ret);
            container.addView(ret);
            return ret;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

}
