package top.scraft.picman2.server.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {

    private int code;
    @NonNull
    private String message;
    @Nullable
    private T data;

}
