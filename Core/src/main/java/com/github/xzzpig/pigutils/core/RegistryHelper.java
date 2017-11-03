package com.github.xzzpig.pigutils.core;

import com.github.xzzpig.pigutils.annotation.API;
import com.github.xzzpig.pigutils.annotation.NotNull;
import com.github.xzzpig.pigutils.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Windows注册表的帮助类
 * 使用仅限于Windows
 */
public class RegistryHelper {
    /**
     * 通过exec读取注册表获取Path
     * Path位置是CurrentUser的
     */
    @API
    public static String getUserPath() throws IOException {
        return getValue("HKEY_CURRENT_USER\\Environment", "Path");
    }

    /**
     * 从注册表获取值
     */
    @API
    public static String getValue(String dir, String key) throws IOException {
        String command = "reg query \"" + dir + "\" /v " + key;
        Process ps = Runtime.getRuntime().exec(command);
        ps.getOutputStream().close();
        InputStreamReader i = new InputStreamReader(ps.getInputStream());
        String line;
        BufferedReader ir = new BufferedReader(i);
        StringBuilder sb = new StringBuilder();
        while ((line = ir.readLine()) != null) {
            sb.append(line);
        }
        String[] paths = sb.toString().split("    ");
        return paths[paths.length - 1];
    }

    /**
     * 通过exec读取注册表获取Path
     * Path位置是System的
     */
    @API
    public static String getSystemPath() throws IOException {
        return getValue("HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment", "Path");
    }

    /**
     * 设置注册表的值
     *
     * @param type is REG_SZ if null
     */
    @API
    public static void setValue(@NotNull String dir, @NotNull String key, @Nullable String type, @NotNull String value) throws IOException {
        deleteValue(dir, key);
        if (type == null)
            type = "REG_SZ";
        String command = "reg add \"" + dir + "\" /v " + key + " /t " + type + " /d " + value;
        Process ps = Runtime.getRuntime().exec(command);
        ps.getOutputStream().close();
    }

    @API
    public static void deleteValue(@NotNull String dir, @NotNull String key) throws IOException {
        String command = "reg delete \"" + dir + "\" /f /v " + key;
        Process ps = Runtime.getRuntime().exec(command);
        ps.getOutputStream().close();
    }

    @API
    public static void addUserPath(@NotNull String path) throws IOException {
        if (hasUserPath(path))
            return;
        String userPath = getUserPath();
        if (!path.matches(".*%\\w+%.*"))
            path = new File(path).getAbsolutePath();
        userPath = userPath + ";" + path + ";";
        userPath = cleanPath(userPath);
        setValue("HKEY_CURRENT_USER\\Environment", "Path", null, userPath);
    }

    @API
    public static void removeUserPath(@NotNull String path) throws IOException {
        String userPath = getUserPath();
        if (!path.matches(".*%\\w+%.*"))
            path = new File(path).getAbsolutePath();
        path = path + ";";
        path = cleanPath(path);
        path = ";" + path;
        userPath = ";" + userPath+";";
        userPath = userPath.replace(path, "");
        userPath = cleanPath(userPath);
        setValue("HKEY_CURRENT_USER\\Environment", "Path", null, userPath);
    }

    @API
    public static boolean hasUserPath(@NotNull String path) throws IOException {
        String userPath = getUserPath();
        if (!path.matches(".*%\\w+%.*"))
            path = new File(path).getAbsolutePath();
        path = path + ";";
        path = cleanPath(path);
        path = ";" + path;
        userPath = ";" + userPath;
        return userPath.contains(path);
    }

    private static String cleanPath(@Nullable String path) {
        while (path.contains(";;")) {
            path = path.replace(";;", ";");
        }
        if(path.startsWith(";"))
            path = path.replaceFirst(";","");
        path = path.replace(".;", ";");
        path = path.replaceAll("\\\\*\\.*;", ";");
        return path;
    }

    /**
     * 设置用户环境变量
     */
    @API
    public static void setUserEnv(@NotNull String key, String value) throws IOException {
        setValue("HKEY_CURRENT_USER\\Environment", key, null, value);
    }

    /**
     * 移除用户环境变量
     */
    @API
    public static void removeUserEnv(@NotNull String key) throws IOException {
        deleteValue("HKEY_CURRENT_USER\\Environment", key);
    }

    public static void main(String[] args) throws IOException {
//        addUserPath("%JAVA_HOME%\\bin");
        removeUserPath("%JAVA_HOME%\\bin");
    }
}
