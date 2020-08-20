package top.scraft.picman2.storage.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;

public class PictureTagConverter implements PropertyConverter<ArrayList<String>, String> {

    private static final Gson GSON = new Gson();
    private static final TypeToken<ArrayList<String>> TYPE_TOKEN = new TypeToken<ArrayList<String>>(){};

    @Override
    public ArrayList<String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return new ArrayList<>();
        } else {
            return GSON.fromJson(databaseValue, TYPE_TOKEN.getType());
        }
    }

    @Override
    public String convertToDatabaseValue(ArrayList<String> entityProperty) {
        if (entityProperty == null) {
            return null;
        } else {
            return GSON.toJson(entityProperty);
        }
    }

}
