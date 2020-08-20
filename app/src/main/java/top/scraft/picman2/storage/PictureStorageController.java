package top.scraft.picman2.storage;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class PictureStorageController {

    private static final String STORAGE_DIRECTORY_PATH = "Kuresaru/Picman2";
    private static final String STORAGE_DIRECTORY_PICTURE_NAME = "Pictures";
    private static final String STORAGE_DIRECTORY_TEMP_NAME = "Temp";

    private static PictureStorageController instance = null;

    private final Context context;

    public static PictureStorageController getInstance(Context appContext) {
        if (instance == null) {
            instance = new PictureStorageController(appContext);
        }
        return instance;
    }

    private PictureStorageController(Context context) {
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

}
