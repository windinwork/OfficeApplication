package com.windinwork.officeapplication;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

public class StorageUtils {

    /**
     * 获取存放文件的文件夹
     * @param context
     * @return
     */
    public static File getFileDir(@NonNull Context context) {
        File cache = context.getExternalCacheDir();
        File tmp = new File(cache, "file");
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        return tmp;
    }

    /**
     * 获取临时文件夹
     * @param context
     * @return
     */
    public static File getTempDir(@NonNull Context context) {
        File cache = context.getExternalCacheDir();
        File tmp = new File(cache, "tmp");
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        return tmp;
    }
}
