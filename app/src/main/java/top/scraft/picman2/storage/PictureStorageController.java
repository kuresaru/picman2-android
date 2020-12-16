package top.scraft.picman2.storage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import top.scraft.picman2.utils.FileUtils;

public class PictureStorageController {

    private static final String STORAGE_DIRECTORY_PICTURE_NAME = "Pictures";
    private static final String STORAGE_DIRECTORY_THUMB_NAME = "Thumbs";

    private static final String STORAGE_DIRECTORY_TEMP_NAME = "Picman2ShareTemp";

    private final Context context;

    PictureStorageController(Context context) {
        this.context = context;
    }

    @Nullable
    private File getStorageDirectory() {
        return context.getExternalFilesDir(null);
    }

    /**
     * 检查数据目录, 不存在时递归创建目录, 存在时检查状态
     *
     * @param file 要检查的目录
     * @return 检查后的目录
     */
    @NonNull
    private File checkDirectory(@NonNull File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(context, "建立目录失败" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } else if (!file.isDirectory()) {
            Toast.makeText(context, "目录状态异常" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    @NonNull
    private File getPictureDirectory() {
        return checkDirectory(new File(getStorageDirectory(), STORAGE_DIRECTORY_PICTURE_NAME));
    }

    @NonNull
    private File getThumbDirectory() {
        return checkDirectory(new File(getStorageDirectory(), STORAGE_DIRECTORY_THUMB_NAME));
    }

    @NonNull
    private File getTempDirectory() {
        return checkDirectory(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), STORAGE_DIRECTORY_TEMP_NAME));
    }

    @NonNull
    public File getThumbPath(@NonNull String pid) {
        return new File(getThumbDirectory(), pid);
    }

    @NonNull
    public File getPicturePath(@NonNull String pid) {
        return new File(getPictureDirectory(), pid);
    }

    /**
     * 源图片保存到图片存储
     *
     * @param src 源图片
     * @param pid 图片id
     * @return 是否保存成功
     */
    public boolean savePicture(@NonNull File src, @NonNull String pid) {
        File dst = getPicturePath(pid);
        try {
            FileUtils.copyFile(src, dst);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 保存临时图片
     *
     * @param pid 图片id
     * @return 是否成功
     */
    public boolean copyTemp(@NonNull String pid) {
        File src = getPicturePath(pid);
        File dst = new File(getTempDirectory(), pid);
        try {
            FileUtils.copyFile(src, dst);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(dst));
            context.sendBroadcast(intent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearTemp() {
        for (File file : getTempDirectory().listFiles()) {
            file.delete();
        }
    }

    public int tempCount() {
        return getTempDirectory().list().length;
    }

}
