package top.scraft.picman2.storage.dao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import top.scraft.picman2.storage.dao.gen.DaoSession;
import top.scraft.picman2.storage.dao.gen.PictureDao;
import top.scraft.picman2.storage.dao.gen.PictureLibraryDao;

@Entity
public class PictureLibrary {

    @Id
    private Long appInternalLid;
    private Long lid;
    @NotNull
    private String name;
    @NotNull
    private Long lastUpdate;
    @ToMany
    @JoinEntity(entity = PiclibPictureMap.class, sourceProperty = "appInternalLid", targetProperty = "appInternalPid")
    private List<Picture> pictures;
    private boolean offline;
    private boolean readonly;


    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 891498684)
    private transient PictureLibraryDao myDao;
    @Generated(hash = 1073290780)
    public PictureLibrary() {
    }
    @Generated(hash = 1525643442)
    public PictureLibrary(Long appInternalLid, Long lid, @NotNull String name, @NotNull Long lastUpdate, boolean offline,
            boolean readonly) {
        this.appInternalLid = appInternalLid;
        this.lid = lid;
        this.name = name;
        this.lastUpdate = lastUpdate;
        this.offline = offline;
        this.readonly = readonly;
    }
    public Long getAppInternalLid() {
        return this.appInternalLid;
    }
    public void setAppInternalLid(Long appInternalLid) {
        this.appInternalLid = appInternalLid;
    }
    public Long getLid() {
        return this.lid;
    }
    public void setLid(Long lid) {
        this.lid = lid;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean getOffline() {
        return this.offline;
    }
    public void setOffline(boolean offline) {
        this.offline = offline;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1283810663)
    public List<Picture> getPictures() {
        if (pictures == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PictureDao targetDao = daoSession.getPictureDao();
            List<Picture> picturesNew = targetDao._queryPictureLibrary_Pictures(appInternalLid);
            synchronized (this) {
                if (pictures == null) {
                    pictures = picturesNew;
                }
            }
        }
        return pictures;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1035739203)
    public synchronized void resetPictures() {
        pictures = null;
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 911225722)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPictureLibraryDao() : null;
    }
    public Long getLastUpdate() {
        return this.lastUpdate;
    }
    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    public boolean getReadonly() {
        return this.readonly;
    }
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

}
