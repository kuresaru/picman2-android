package top.scraft.picman2.storage;

import android.content.Context;
import android.content.SharedPreferences;

import lombok.Getter;
import top.scraft.picman2.storage.dao.StorageOpenHelper;
import top.scraft.picman2.storage.dao.gen.DaoMaster;
import top.scraft.picman2.storage.dao.gen.DaoSession;

public class PicmanStorage {

    private static PicmanStorage INSTANCE = null;

    // simple storage
    private final SharedPreferences userdata;
    @Getter
    private final PictureStorageController pictureStorage;
    // database
    private final DaoMaster.OpenHelper openHelper;
    private final DaoMaster daoMaster;
    @Getter
    private final DaoSession daoSession;

    private PicmanStorage(Context appContext) {
        // simple storage
        userdata = appContext.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        pictureStorage = new PictureStorageController(appContext);
        // database
        openHelper = new StorageOpenHelper(appContext);
        daoMaster = new DaoMaster(openHelper.getWritableDb());
        daoSession = daoMaster.newSession();
    }

    public static PicmanStorage getInstance(Context appContext) {
        if (INSTANCE == null) {
            synchronized (PicmanStorage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PicmanStorage(appContext);
                }
            }
        }
        return INSTANCE;
    }

}
