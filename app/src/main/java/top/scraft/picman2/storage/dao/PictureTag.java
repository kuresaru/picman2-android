package top.scraft.picman2.storage.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class PictureTag {

    @Id
    private Long appInternalTid;
    private Long appInternalPid;
    private String tag;


    @Generated(hash = 791444159)
    public PictureTag(Long appInternalTid, Long appInternalPid, String tag) {
        this.appInternalTid = appInternalTid;
        this.appInternalPid = appInternalPid;
        this.tag = tag;
    }
    @Generated(hash = 2119036990)
    public PictureTag() {
    }
    public Long getAppInternalTid() {
        return this.appInternalTid;
    }
    public void setAppInternalTid(Long appInternalTid) {
        this.appInternalTid = appInternalTid;
    }
    public Long getAppInternalPid() {
        return this.appInternalPid;
    }
    public void setAppInternalPid(Long appInternalPid) {
        this.appInternalPid = appInternalPid;
    }
    public String getTag() {
        return this.tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

}
