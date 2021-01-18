package top.scraft.picman2.server.data;

import java.util.Set;

import lombok.Data;

@Data
public class UpdatePictureRequest {

    private String description;
    private Set<String> tags;

}
