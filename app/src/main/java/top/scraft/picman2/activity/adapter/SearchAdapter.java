package top.scraft.picman2.activity.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;

import top.scraft.picman2.activity.MainActivity;
import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;
import top.scraft.picman2.server.ServerController;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.dao.PiclibPictureMap;
import top.scraft.picman2.storage.dao.Picture;
import top.scraft.picman2.storage.dao.PictureLibrary;
import top.scraft.picman2.storage.dao.gen.PiclibPictureMapDao;
import top.scraft.picman2.storage.dao.gen.PictureLibraryDao;

public class SearchAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private final MainActivity mainActivity;
    private final List<Picture> pictures;
    private final ServerController serverController;
    private final PicmanStorage picmanStorage;

    public SearchAdapter(MainActivity mainActivity, List<Picture> pictures) {
        this.mainActivity = mainActivity;
        this.pictures = pictures;
        this.serverController = ServerController.getInstance(mainActivity);
        this.picmanStorage = PicmanStorage.getInstance(mainActivity);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Picture picture = pictures.get(position);
        File thumb = picmanStorage.getPictureStorage().getThumbPath(picture.getPid());
        if (thumb.exists()) {
            holder.setImage(thumb);
        } else {
            new Thread(() -> {
                File pf = picmanStorage.getPictureStorage().getPicturePath(picture.getPid());
                if (pf.exists()) {
                    // TODO 生成
                } else {
                    QueryBuilder<PiclibPictureMap> lpmQuery = picmanStorage.getDaoSession().getPiclibPictureMapDao().queryBuilder();
                    lpmQuery.where(PiclibPictureMapDao.Properties.AppInternalPid.eq(picture.getAppInternalPid())).join(
                            PiclibPictureMapDao.Properties.AppInternalLid,
                            PictureLibrary.class,
                            PictureLibraryDao.Properties.AppInternalLid
                    ).where(PictureLibraryDao.Properties.Offline.eq(false));
                    List<PiclibPictureMap> accessLibs = lpmQuery.list();
                    boolean saved = false;
                    for (PiclibPictureMap accessLib : accessLibs) {
                        PictureLibrary lib = picmanStorage.getDaoSession().getPictureLibraryDao().load(accessLib.getAppInternalLid());
                        if (serverController.savePictureThumb(lib.getLid(), picture.getPid(), thumb)) {
                            saved = true;
                            break;
                        }
                    }
                    if (saved) {
                        mainActivity.runOnUiThread(() -> holder.setImage(thumb));
                    }
                }
            }).start();
            holder.setImage(null);
        }
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

}
