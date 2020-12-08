package top.scraft.picman2.storage.dao;

import org.greenrobot.greendao.annotation.*;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.DaoException;
import top.scraft.picman2.storage.dao.gen.DaoSession;
import top.scraft.picman2.storage.dao.gen.PictureLibraryDao;
import top.scraft.picman2.storage.dao.gen.PictureDao;

@Entity
public class Picture {

    @Id
    private Long appInternalPid;
    @NotNull
    private String pid;
    @NotNull
    private Long createTime;
    private String creator;
    @NotNull
    private String description;
    @NotNull
    private Long fileSize;
    @NotNull
    private Integer height;
    @NotNull
    private Integer width;
    @NotNull
    private Boolean valid;
    @NotNull
    private Long lastModify;
    @Convert(columnType = String.class, converter = PictureTagConverter.class)
    private ArrayList<String> tags;
    @ToMany
    @JoinEntity(entity = PiclibPictureMap.class, sourceProperty = "appInternalPid", targetProperty = "appInternalLid")
    private List<PictureLibrary> libraries;


    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 220989104)
    private transient PictureDao myDao;
    @Generated(hash = 796325093)
    public Picture(Long appInternalPid, @NotNull String pid, @NotNull Long createTime, String creator,
            @NotNull String description, @NotNull Long fileSize, @NotNull Integer height, @NotNull Integer width,
            @NotNull Boolean valid, @NotNull Long lastModify, ArrayList<String> tags) {
        this.appInternalPid = appInternalPid;
        this.pid = pid;
        this.createTime = createTime;
        this.creator = creator;
        this.description = description;
        this.fileSize = fileSize;
        this.height = height;
        this.width = width;
        this.valid = valid;
        this.lastModify = lastModify;
        this.tags = tags;
    }
    @Generated(hash = 1602548376)
    public Picture() {
    }
    public Long getAppInternalPid() {
        return this.appInternalPid;
    }
    public void setAppInternalPid(Long appInternalPid) {
        this.appInternalPid = appInternalPid;
    }
    public String getPid() {
        return this.pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }
    public Long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    public String getCreator() {
        return this.creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getFileSize() {
        return this.fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public Integer getHeight() {
        return this.height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    public Integer getWidth() {
        return this.width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public Boolean getValid() {
        return this.valid;
    }
    public void setValid(Boolean valid) {
        this.valid = valid;
    }
    public Long getLastModify() {
        return this.lastModify;
    }
    public void setLastModify(Long lastModify) {
        this.lastModify = lastModify;
    }
    public ArrayList<String> getTags() {
        return this.tags;
    }
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1710405637)
    public List<PictureLibrary> getLibraries() {
        if (libraries == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PictureLibraryDao targetDao = daoSession.getPictureLibraryDao();
            List<PictureLibrary> librariesNew = targetDao._queryPicture_Libraries(appInternalPid);
            synchronized (this) {
                if (libraries == null) {
                    libraries = librariesNew;
                }
            }
        }
        return libraries;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 669042719)
    public synchronized void resetLibraries() {
        libraries = null;
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
    @Generated(hash = 1412175658)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPictureDao() : null;
    }

}
