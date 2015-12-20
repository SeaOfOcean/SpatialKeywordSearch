package util;

/**
 * Created by xianyan on 7/7/15.
 */

import org.apache.commons.lang.SystemUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Property {
    static Properties prop = new Properties();
    static String projectRoot;
    static String propFileName;
    static String resourceRoot;

    static {
        String resourceLabel = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            projectRoot = System.getProperty("user.dir") + "/";
            resourceLabel = "resource_root_windows";
        } else if (SystemUtils.IS_OS_LINUX) {
            projectRoot = "/users/jiaxianyan/project/";
            resourceLabel = "resource_root_linux";
        } else if (SystemUtils.IS_OS_MAC) {
            projectRoot = System.getProperty("user.dir") + "/";
            resourceRoot = null;
        } else {
            throw new UnsupportedOperationException();
        }
        String propFileName = projectRoot + "config.properties";
        try {
            prop.load(new FileInputStream(propFileName));
            resourceRoot = Property.getProperty(resourceLabel);
            System.out.println("load property file " + propFileName + " success");
        } catch (IOException e) {
            System.err.println("Property file not found!");
        }
    }

    public static String getProperty(String key) {
        return prop.getProperty(key);
    }

    public static String getResource(String key) {
        return resourceRoot + getProperty(key);
    }


    public static int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key).trim());
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public static Double getDoubleProperty(String key) {
        return Double.parseDouble(getProperty(key));
    }

    public static Float getFloatProperty(String key) {
        return Float.parseFloat(getProperty(key));
    }
}
