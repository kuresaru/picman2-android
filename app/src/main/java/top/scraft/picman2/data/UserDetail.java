package top.scraft.picman2.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetail {

    private boolean loggedIn;
    private String sacLoginUrl;
    private String username;
    private boolean admin;

}
