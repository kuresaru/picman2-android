package top.scraft.picman2.activity.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.greenrobot.greendao.query.QueryBuilder;

import java.io.File;
import java.util.List;
import java.util.Locale;

import top.scraft.picman2.R;
import top.scraft.picman2.activity.MainActivity;
import top.scraft.picman2.activity.adapter.viewholder.ImageViewHolder;
import top.scraft.picman2.server.ServerController;
import top.scraft.picman2.storage.PicmanStorage;
import top.scraft.picman2.storage.dao.PiclibPictureMap;
import top.scraft.picman2.storage.dao.Picture;
import top.scraft.picman2.storage.dao.PictureLibrary;
import top.scraft.picman2.storage.dao.PictureTag;
import top.scraft.picman2.storage.dao.gen.PiclibPictureMapDao;
import top.scraft.picman2.storage.dao.gen.PictureLibraryDao;
import top.scraft.picman2.storage.dao.gen.PictureTagDao;

public class SearchAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private final MainActivity mainActivity;
    private final List<Picture> pictures;
    private final ServerController serverController;
    private final PicmanStorage picmanStorage;
    private final LayoutInflater layoutInflater;

    public SearchAdapter(MainActivity mainActivity, List<Picture> pictures) {
        this.mainActivity = mainActivity;
        this.pictures = pictures;
        this.serverController = ServerController.getInstance(mainActivity);
        this.picmanStorage = PicmanStorage.getInstance(mainActivity);
        layoutInflater = LayoutInflater.from(mainActivity);
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
            setGalleryImageClick(holder, picture);
        } else {
            new Thread(() -> {
                File pf = picmanStorage.getPictureStorage().getPicturePath(picture.getPid());
                if (pf.exists()) {
                    Bitmap src;
                    if (pf.getName().endsWith(".gif")) {
                        Movie movie = Movie.decodeFile(pf.getAbsolutePath());
                        if (movie == null || movie.width() < 1 || movie.height() < 1) {
                            return;
                        }
                        src = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(src);
                        movie.draw(canvas, 0, 0);
                        canvas.save();
                    } else {
                        src = BitmapFactory.decodeFile(pf.getAbsolutePath());
                    }
                    if (src != null && src.getWidth() > 0 && src.getHeight() > 0) {
                        Bitmap dst = ThumbnailUtils.extractThumbnail(src, 200, 200);
                        src.recycle();
                        mainActivity.runOnUiThread(() -> {
                            holder.setImage(dst);
                            setGalleryImageClick(holder, picture);
                        });
                    }
                } else {
                    List<PiclibPictureMap> accessLibs = getPictureAccessLibIlid(picture.getAppInternalPid());
                    boolean saved = false;
                    for (PiclibPictureMap accessLib : accessLibs) {
                        PictureLibrary lib = picmanStorage.getDaoSession().getPictureLibraryDao().load(accessLib.getAppInternalLid());
                        if (serverController.savePictureFile(lib.getLid(), picture.getPid(), thumb, true)) {
                            saved = true;
                            break;
                        }
                    }
                    if (saved) {
                        mainActivity.runOnUiThread(() -> {
                            holder.setImage(thumb);
                            setGalleryImageClick(holder, picture);
                        });
                    }
                }
            }).start();
            holder.setImage((File) null);
        }
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    private void setGalleryImageClick(@NonNull ImageViewHolder holder, Picture picture) {
        holder.getView().setOnClickListener(v -> {
            final View contentView = layoutInflater.inflate(R.layout.dialog_search_result_info_action, null);
            final ImageView imageView = contentView.findViewById(R.id.imageView);
            final ChipGroup chipGroup = contentView.findViewById(R.id.chipGroup);
            // 加载图片
            File file = picmanStorage.getPictureStorage().getPicturePath(picture.getPid());
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file));
            } else {
                Toast.makeText(mainActivity, "正在下载原图", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    List<PiclibPictureMap> accessLibs = getPictureAccessLibIlid(picture.getAppInternalPid());
                    boolean saved = false;
                    for (PiclibPictureMap accessLib : accessLibs) {
                        PictureLibrary lib = picmanStorage.getDaoSession().getPictureLibraryDao().load(accessLib.getAppInternalLid());
                        if (serverController.savePictureFile(lib.getLid(), picture.getPid(), file, false)) {
                            saved = true;
                            break;
                        }
                    }
                    boolean finalSaved = saved;
                    mainActivity.runOnUiThread(() -> {
                        if (finalSaved) {
                            imageView.setImageURI(Uri.fromFile(file));
                        } else {
                            Toast.makeText(mainActivity, "下载原图失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
            // 加载标签
            List<PictureTag> tags = picmanStorage.getDaoSession().getPictureTagDao().queryBuilder()
                    .where(PictureTagDao.Properties.AppInternalPid.eq(picture.getAppInternalPid()))
                    .list();
            for (PictureTag tag : tags) {
                View chipView = layoutInflater.inflate(R.layout.item_pictire_editor_tag, null);
                Chip chip = chipView.findViewById(R.id.item_picture_editor_tag);
                chip.setText(tag.getTag());
                chipGroup.addView(chipView);
            }
            final AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
            dialog.setTitle(picture.getDescription());
            dialog.setView(contentView);
            dialog.setPositiveButton(R.string.text_share, (d, w) -> {
                final Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(mainActivity, "top.scraft.picman2.fileProvider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                mainActivity.startActivity(Intent.createChooser(intent, "分享图片: ".concat(picture.getDescription())));
            });
            dialog.setNegativeButton(R.string.text_save_tmp, (d, w) -> saveTemp(picture.getPid()));
            dialog.setNeutralButton(R.string.text_clear_save_tmp, (d, w) -> {
                picmanStorage.getPictureStorage().clearTemp();
                saveTemp(picture.getPid());
            });
            dialog.show();
        });
    }

    private List<PiclibPictureMap> getPictureAccessLibIlid(Long ipid) {
        QueryBuilder<PiclibPictureMap> lpmQuery = picmanStorage.getDaoSession().getPiclibPictureMapDao().queryBuilder();
        lpmQuery.where(PiclibPictureMapDao.Properties.AppInternalPid.eq(ipid)).join(
                PiclibPictureMapDao.Properties.AppInternalLid,
                PictureLibrary.class,
                PictureLibraryDao.Properties.AppInternalLid
        ).where(PictureLibraryDao.Properties.Offline.eq(false));
        return lpmQuery.list();
    }

    private void saveTemp(String pid) {
        if (picmanStorage.getPictureStorage().copyTemp(pid)) {
            int count = picmanStorage.getPictureStorage().tempCount();
            Toast.makeText(mainActivity, String.format(Locale.CHINESE, "保存成功, 现共%d张图片", count), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mainActivity, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

}
