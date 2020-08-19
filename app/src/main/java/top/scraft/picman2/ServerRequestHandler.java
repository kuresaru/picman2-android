package top.scraft.picman2;

import okhttp3.ResponseBody;

@FunctionalInterface
public interface ServerRequestHandler {

    void handle(int code, ResponseBody body, Exception e);

}
