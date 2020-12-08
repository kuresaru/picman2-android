package top.scraft.picman2.storage;

import android.content.Context;
import android.content.SharedPreferences;
import lombok.Getter;
import top.scraft.picman2.storage.dao.StorageOpenHelper;
import top.scraft.picman2.storage.dao.gen.DaoMaster;
import top.scraft.picman2.storage.dao.gen.DaoSession;
import top.scraft.picman2.storage.dao.gen.PictureDao;
import top.scraft.picman2.storage.dao.gen.PictureLibraryDao;

public class PicmanStorage {

    private static PicmanStorage INSTANCE = null;

    // simple storage
    private final SharedPreferences userdata;
    @Getter
    private final PictureStorageController pictureStorage;
    // database
    private final DaoMaster.OpenHelper openHelper;
    private final DaoMaster daoMaster;
    private final DaoSession daoSession;
    @Getter
    private final PictureLibraryDao pictureLibraryDao;
    @Getter
    private final PictureDao pictureDao;

    private PicmanStorage(Context appContext) {
        // simple storage
        userdata = appContext.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        pictureStorage = new PictureStorageController(appContext);
        // database
        openHelper = new StorageOpenHelper(appContext);
        daoMaster = new DaoMaster(openHelper.getWritableDb());
        daoSession = daoMaster.newSession();
        pictureLibraryDao = daoSession.getPictureLibraryDao();
        pictureDao = daoSession.getPictureDao();
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
