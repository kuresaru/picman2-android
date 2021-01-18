package top.scraft.picman2.server.data;

import lombok.Data;

@Data
public class LibDetails {

    private long lid;
    private String name;
    private int picCount;
    private long lastUpdate;
    private boolean readonly;

}
