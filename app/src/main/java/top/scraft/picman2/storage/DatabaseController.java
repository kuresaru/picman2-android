package top.scraft.picman2.storage;

import android.content.Context;
import top.scraft.picman2.storage.dao.gen.DaoMaster;
import top.scraft.picman2.storage.dao.gen.DaoSession;

public class DatabaseController {

    private static DatabaseController instance = null;

    private final DaoMaster daoMaster;
    private final DaoSession daoSession;

    public static DatabaseController getInstance(Context appContext) {
        if (instance == null) {
            instance = new DatabaseController(appContext);
        }
        return instance;
    }

    private DatabaseController(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "picman2.db", null);
        daoMaster = new DaoMaster(helper.getWritableDb());
        daoSession = daoMaster.newSession();
    }

}
