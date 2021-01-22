package top.scraft.picman2.server;

import okhttp3.ResponseBody;

@FunctionalInterface
@Deprecated
public interface ServerRequestHandler {

    void handle(int code, ResponseBody body, Exception e);

}
