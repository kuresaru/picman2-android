package top.scraft.picman2.utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static void saveFileFromStream(@NonNull InputStream src, @NonNull File dst) throws IOException {
        int len;
        byte[] buf = new byte[4096];
        FileOutputStream outputStream = new FileOutputStream(dst, false);
        while ((len = src.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
    }

    public static void copyFile(@NonNull File src, @NonNull File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        FileUtils.saveFileFromStream(in, dst);
        in.close();
    }

}
