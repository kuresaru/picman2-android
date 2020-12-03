package top.scraft.picman2.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

}
