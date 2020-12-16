package top.scraft.picman2.utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.SneakyThrows;

public class FileUtils {

    @SneakyThrows(NoSuchAlgorithmException.class)
    public static String fileMD5(File file) {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int len;
            byte[] buf = new byte[1024];
            while ((len = inputStream.read(buf)) != -1) {
                digest.update(buf, 0, len);
            }
            inputStream.close();
            byte[] md5 = digest.digest();
            StringBuilder ret = new StringBuilder();
            for (byte b : md5) {
                ret.append(String.format("%02x", ((int) b) & 0xFF));
            }
            return ret.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
