package top.scraft.picman2.activity.adapter;

import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;
import top.scraft.picman2.R;
import top.scraft.picman2.activity.PicLibManagerActivity;
import top.scraft.picman2.server.ServerController;
import top.scraft.picman2.server.data.LibDetails;
import top.scraft.picman2.server.data.PictureDetail;
import top.scraft.picman2.server.data.Result;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.PictureStorageController;
import top.scraft.picman2.storage.dao.PiclibPictureMap;
import top.scraft.picman2.storage.dao.Picture;
import top.scraft.picman2.storage.dao.PictureLibrary;
import top.scraft.picman2.storage.dao.gen.PiclibPictureMapDao;
import top.scraft.picman2.storage.dao.gen.PictureDao;
import top.scraft.picman2.storage.dao.gen.PictureLibraryDao;

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
    private final AlertDialog superDialog;
    
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
          final ServerController serverController = ServerController.getInstance(context.getApplicationContext());
          final PicmanStorage picmanStorage = PicmanStorage.getInstance(context.getApplicationContext());
          final PictureStorageController storageController = picmanStorage.getPictureStorage();
          final PictureLibraryDao lDao = picmanStorage.getDaoSession().getPictureLibraryDao();
          final PictureDao pDao = picmanStorage.getDaoSession().getPictureDao();
          final PiclibPictureMapDao lpmDao = picmanStorage.getDaoSession().getPiclibPictureMapDao();
          
          String selectedOption = (String) options.get(position1).get("option");
          if (selectedOption != null) {
            if ("删除".equals(selectedOption)) {
              AlertDialog.Builder confirm = new AlertDialog.Builder(context);
              confirm.setTitle("删除图库");
              confirm.setMessage(String.format("确认删除图库[%s]?", library.getName()));
              confirm.setNegativeButton(R.string.text_cancel, null);
              confirm.setPositiveButton(R.string.text_delete, (d, w) -> new Thread(() -> {
                // 删除服务器上的在线图库
                if (!library.getOffline()) {
                  String error = serverController.deleteLibrary(library.getLid());
                  if (error != null) {
                    context.runOnUiThread(() -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show());
                    return;
                  }
                }
                // 删除本地图库
                lDao.deleteByKey(library.getAppInternalLid());
                Iterator<PictureLibrary> itr = infoList.iterator();
                while (itr.hasNext()) {
                  PictureLibrary l = itr.next();
                  if (l.getAppInternalLid().equals(library.getAppInternalLid())) {
                    itr.remove();
                    // 删除映射
                    lpmDao.queryBuilder()
                        .where(PiclibPictureMapDao.Properties.AppInternalLid.eq(l.getAppInternalLid()))
                        .buildDelete().executeDeleteWithoutDetachingEntities();
                  }
                }
                context.runOnUiThread(PiclibManagerAdapter.this::notifyDataSetChanged);
                superDialog.dismiss();
              }).start());
              confirm.show();
            } else if (selectedOption.startsWith("转换为")) {
              if (library.getOffline()) {
                // 离线转在线 上传本地到服务器
                new AlertDialog.Builder(context)
                    .setTitle("转换为在线")
                    .setMessage(String.format(Locale.CHINA,
                        "转换%s为在图库, 该操作会上传图库内容到服务器.\n" +
                            "!!!注意: 测试功能, 可能会丢失数据.\n" +
                            "服务器已存在的图片以服务器为准.\n" +
                            "上传完成后原离线图库继续保留.",
                        library.getName()))
                    .setNegativeButton(R.string.text_cancel, null)
                    .setPositiveButton(R.string.text_confirm, (_d, _w) -> {
                      ProgressDialog progressDialog = new ProgressDialog(context);
                      progressDialog.setTitle(String.format(Locale.CHINA, "上传%s到服务器", library.getName()));
                      progressDialog.setMessage("创建在线图库");
                      progressDialog.setCancelable(false);
                      PictureLibrary oldLib = lDao.load(this.library.getAppInternalLid()); // 传过来的Lib已经没有Session 需要重新查询
                      List<Picture> pictureList = oldLib.getPictures();
                      final int total = oldLib.getPictures().size() + 1;
                      final AtomicInteger progress = new AtomicInteger(0);
                      progressDialog.setMax(total);
                      progressDialog.setProgress(0);
                      progressDialog.show();
                      new Thread(() -> {
                        // 创建新在线图库
                        Result<LibDetails> newLibDetails = serverController.createLibrary(oldLib.getName());
                        if (newLibDetails.getCode() != 200) {
                          String error = newLibDetails.getMessage();
                          context.runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                          });
                          return;
                        }
                        PictureLibrary newLib = new PictureLibrary();
                        assert (newLibDetails.getData() != null);
                        newLib.setLid(newLibDetails.getData().getLid());
                        newLib.setName(newLibDetails.getData().getName());
                        newLib.setLastUpdate(newLibDetails.getData().getLastUpdate());
                        lDao.save(newLib);
                        context.runOnUiThread(context::updateData);
                        while (progress.get() < pictureList.size()) {
                          int p = progress.incrementAndGet();
                          Picture picture = pDao.load(pictureList.get(p - 1).getAppInternalPid());
                          context.runOnUiThread(() -> {
                            progressDialog.setProgress(p);
                            progressDialog.setMessage("上传图片 " + picture.getDescription());
                          });
                          Result<PictureDetail> serverPictureDetailResult = serverController.getPictureMeta(picture.getPid());
                          boolean needUpload;
                          if (serverPictureDetailResult.getCode() == 404) {
                            // 不存在
                            Result<PictureDetail> newPicDetail = serverController.updatePictureMeta(newLib.getLid(),
                                picture.getPid(), picture.getDescription(), picture.getTagsAsStringSet());
                            if (newPicDetail.getCode() == 200 && newPicDetail.getData() != null) {
                              needUpload = !newPicDetail.getData().isValid();
                            } else {
                              // 发生错误 可能是满了
                              context.runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(context, "创建失败: " +
                                    serverPictureDetailResult.getMessage(), Toast.LENGTH_SHORT).show();
                              });
                              return; // 满了不要再继续上传
                            }
                          } else if (serverPictureDetailResult.getCode() == 200 && serverPictureDetailResult.getData() != null) {
                            // 存在 检查
                            needUpload = !serverPictureDetailResult.getData().isValid();
                          } else {
                            // 发生错误
                            context.runOnUiThread(() -> Toast.makeText(context,
                                serverPictureDetailResult.getMessage(), Toast.LENGTH_SHORT).show());
                            continue;
                          }
                          if (needUpload) {
                            Result<Object> uploadResult = serverController.uploadPicture(newLib.getLid(), picture.getPid(),
                                storageController.getPicturePath(picture.getPid()));
                            if (uploadResult.getCode() != 200) {
                              context.runOnUiThread(() -> Toast.makeText(context, "上传失败: " +
                                  uploadResult.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                          }
                          // 上传完成 保存到本地
                          PiclibPictureMap lpMap = new PiclibPictureMap(null,
                              newLib.getAppInternalLid(), picture.getAppInternalPid());
                          lpmDao.save(lpMap);
                        }
                        context.runOnUiThread(() -> {
                          progressDialog.dismiss();
                          context.updateData();
                          Toast.makeText(context, "上传完成", Toast.LENGTH_SHORT).show();
                        });
                      }).start();
                      superDialog.dismiss();
                    })
                    .show();
              } else {
                // 在线转离线 删除服务器图库
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("转换为离线");
                builder.setMessage(String.format(Locale.CHINA,
                    "转换%s为离线图库, 该操作会删除服务器上的图库.",
                    library.getName()));
                builder.setNegativeButton(R.string.text_cancel, null);
                builder.setPositiveButton(R.string.text_confirm, (d, w) -> new Thread(() -> {
                  String error = serverController.deleteLibrary(library.getLid());
                  if (error != null) {
                    context.runOnUiThread(() -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show());
                    return;
                  }
                  library.setOffline(true);
                  lDao.save(library);
                  context.runOnUiThread(() -> {
                    Toast.makeText(context, "转换完成", Toast.LENGTH_SHORT).show();
                    context.updateData();
                  });
                  superDialog.dismiss();
                }).start());
                builder.show();
              }
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
