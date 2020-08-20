package top.scraft.picman2.storage.dao;

import org.greenrobot.greendao.annotation.*;

import java.util.ArrayList;

@Entity
public class Picture {

    @Id
    private String pid;

    private Long createTime;
    @NotNull
    private String creator;
    @NotNull
    private String description;
    private Long fileSize;
    private Integer height;
    private Integer width;
    private Boolean valid;
    private Long lastModify;
    @Convert(columnType = String.class, converter = PictureTagConverter.class)
    private ArrayList<String> tags;

    @Generated(hash = 1748975410)
    public Picture(String pid, Long createTime, @NotNull String creator,
                   @NotNull String description, Long fileSize, Integer height,
                   Integer width, Boolean valid, Long lastModify, ArrayList<String> tags) {
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

}
