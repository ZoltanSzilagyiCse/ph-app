package hu.sherad.hos.utils;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.sherad.hos.data.models.Topic;

public class Util {


    public static List<String> convertTopicsToNameList(List<Topic> topics) {
        List<String> array = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            array.add(topics.get(i).getTitle());
        }
        return array;
    }

    public static boolean isAppOld(Context context) {
        File file = context.getDatabasePath("proforum.db");
        return file.exists();
    }

}
