package top.scraft.picman2.server;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ServerState {

    VERSION_MISMATCH("服务器版本不匹配", false, false),
    CONNECT_ERROR("服务器连接失败", false, false),
    NOT_LOGIN("未登录", true, false),
    LOGIN("已登录", true, true);

    public final String message;
    public final boolean valid;
    public final boolean login;

}
