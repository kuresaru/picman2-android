package top.scraft.picman2.storage;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PictureStorageController {

    private static final String STORAGE_DIRECTORY_PATH = "Kuresaru/Picman2";
    private static final String STORAGE_DIRECTORY_PICTURE_NAME = "Pictures";
    private static final String STORAGE_DIRECTORY_TEMP_NAME = "Temp";

    private final Context context;

    PictureStorageController(Context context) {
        this.context = context;
    }

    private File getStorageDirectory() {
        return new File(Environment.getExternalStorageDirectory(), STORAGE_DIRECTORY_PATH);
    }

    /**
     * 检查数据目录, 不存在时递归创建目录, 存在时检查状态
     *
     * @param file 要检查的目录
     * @return 检查后的目录
     */
    private File checkDirectory(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(context, "建立目录失败" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } else if (!file.isDirectory()) {
            Toast.makeText(context, "目录状态异常" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    private File getPictureDirectory() {
        return checkDirectory(new File(getStorageDirectory(), STORAGE_DIRECTORY_PICTURE_NAME));
    }

    private File getTempDirectory() {
        return checkDirectory(new File(getStorageDirectory(), STORAGE_DIRECTORY_TEMP_NAME));
    }

    public File getPicturePath(String pid) {
        return new File(getPictureDirectory(), pid);
    }

    /**
     * 源图片保存到图片存储
     *
     * @param src 源图片
     * @param pid 图片id
     * @return 是否保存成功
     */
    public boolean savePicture(File src, String pid) {
        File dst = getPicturePath(pid);
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                out.flush();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
