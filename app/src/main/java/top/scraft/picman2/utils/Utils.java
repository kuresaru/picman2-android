package top.scraft.picman2.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.SneakyThrows;
import okhttp3.MediaType;

public class Utils {
  
  @SneakyThrows(NoSuchAlgorithmException.class)
  public static String md5(byte[] bytes) {
    MessageDigest digest = MessageDigest.getInstance("MD5");
    digest.update(bytes, 0, bytes.length);
    byte[] md5 = digest.digest();
    StringBuilder ret = new StringBuilder();
    for (byte b : md5) {
      ret.append(String.format("%02x", ((int) b) & 0xFF));
    }
    return ret.toString();
  }
  
  public static byte[] readStream(InputStream stream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    int len;
    byte[] buf = new byte[1024];
    while ((len = stream.read(buf)) != -1) {
      outputStream.write(buf, 0, len);
    }
    return outputStream.toByteArray();
  }
  
  public static byte[] readContent(ContentResolver contentResolver, Uri uri) throws IOException {
    InputStream inputStream = contentResolver.openInputStream(uri);
    byte[] bytes = readStream(inputStream);
    inputStream.close();
    return bytes;
  }
  
  /**
   * 接收到的分享 content:// 转为文件路径 失败返回null
   */
  public static String contentToFilePath(ContentResolver contentResolver, Uri uri) {
    String path = null;
    Cursor cursor = contentResolver.query(uri, new String[]{
        MediaStore.Images.ImageColumns.DATA
    }, null, null, null);
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        if (idx > -1) {
          path = cursor.getString(idx);
        }
      }
      cursor.close();
    }
    return path;
  }
  
  public static MediaType mediaType(String pid) {
    MediaType mediaType;
    String pidLower = pid.toLowerCase();
    if (pidLower.endsWith(".gif")) {
      mediaType = MediaType.parse("image/gif");
    } else if (pidLower.endsWith(".png")) {
      mediaType = MediaType.parse("image/png");
    } else {
      mediaType = MediaType.parse("image/jpeg");
    }
    return mediaType;
  }
  
  public static void toastThread(Activity activity, String message) {
    activity.runOnUiThread(() -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
  }
  
}
