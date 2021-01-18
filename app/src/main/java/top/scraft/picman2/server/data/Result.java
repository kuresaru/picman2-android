package top.scraft.picman2.server.data;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

}
