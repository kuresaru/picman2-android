package top.scraft.picman2.storage.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class PiclibPictureMap {

    @Id
    private Long appInternalMapId;
    private Long appInternalLid;
    private Long appInternalPid;


    @Generated(hash = 1210753278)
    public PiclibPictureMap(Long appInternalMapId, Long appInternalLid,
            Long appInternalPid) {
        this.appInternalMapId = appInternalMapId;
        this.appInternalLid = appInternalLid;
        this.appInternalPid = appInternalPid;
    }
    @Generated(hash = 1299523015)
    public PiclibPictureMap() {
    }
    public Long getAppInternalMapId() {
        return this.appInternalMapId;
    }
    public void setAppInternalMapId(Long appInternalMapId) {
        this.appInternalMapId = appInternalMapId;
    }
    public Long getAppInternalLid() {
        return this.appInternalLid;
    }
    public void setAppInternalLid(Long appInternalLid) {
        this.appInternalLid = appInternalLid;
    }
    public Long getAppInternalPid() {
        return this.appInternalPid;
    }
    public void setAppInternalPid(Long appInternalPid) {
        this.appInternalPid = appInternalPid;
    }

}
