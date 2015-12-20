package util;

import com.google.gson.Gson;

import java.io.*;

/**
 * Created by Xianyan Jia on 15/11/2015.
 */
public class GsonUtil {
    public static void save(Object object, String saveFile) {
        Gson gson = new Gson();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
            writer.write(gson.toJson(object));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object load(String saveFile, Class cl) {
        Gson gson = new Gson();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(saveFile));
            String content = reader.readLine();
            return gson.fromJson(content, cl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
