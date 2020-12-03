package top.scraft.picman2.data;

import lombok.Data;

import java.util.Set;

@Data
public class PicLibDetail {

    private int lid;
    private String name;
    private String owner;
    private Set<String> users;
    private int picCount;

}
