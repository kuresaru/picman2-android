package top.scraft.picman2.storage.dao;

import android.content.Context;
import org.greenrobot.greendao.database.Database;
import top.scraft.picman2.storage.dao.gen.DaoMaster;

public class StorageOpenHelper extends DaoMaster.OpenHelper {

    public StorageOpenHelper(Context context) {
        super(context, "picman2.db");
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
    }

}
