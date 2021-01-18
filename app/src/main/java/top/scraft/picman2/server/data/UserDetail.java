package top.scraft.picman2.server.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetail {

    private boolean admin;
    private SacUserPrincipal sacUserPrincipal;

    @Data
    public static class SacUserPrincipal {
        private long said;
        private String username;
        private String nickname;
    }

}
