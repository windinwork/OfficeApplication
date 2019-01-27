package com.windinwork.officeapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.b.a.f;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TbsReaderAssist {

    public static int openFileReader(Context var0, String var1, HashMap<String, String> var2, ValueCallback<String> var3) {
        if (var1 != null) {
            String var4 = var1.substring(var1.lastIndexOf(".") + 1, var1.length());
            if (!a(var0, var1, var4)) {
                openFileReaderListWithQBDownload(var0, var1, var3);
                return 3;
            }

            if (startQBForDoc(var0, var1, 4, 0, var4, a(var0, (Map) var2))) {
                if (var3 != null) {
                    var3.onReceiveValue("open QB");
                }
                return 1;
            }

            Log.d("QbSdk", "openFileReader startQBForDoc return false");
        } else {
            Log.d("QbSdk", "openFileReader QQ browser not installed");
        }

        if (var2 == null) {
            var2 = new HashMap();
        }

        var2.put("local", "true");
        openFileReaderListWithQBDownload(var0, var1, var3);
        return 3;
    }

    public static boolean startQBForDoc(Context var0, String var1, int var2, int var3, String var4, Bundle var5) {
        HashMap var6 = new HashMap();
        var6.put("ChannelID", var0.getApplicationContext().getApplicationInfo().processName);
        var6.put("PosID", Integer.toString(var2));
        return a(var0, var1, var3, var4, var6, var5);
    }

    private static boolean a(Context var0, String var1, String var2) {
        Intent var3 = new Intent("com.tencent.QQBrowser.action.sdk.document");
        var3.setDataAndType(fromFile(var0, new File(var1)), "mtt/" + var2);
        List var4 = var0.getPackageManager().queryIntentActivities(var3, 0);
        boolean var5 = false;
        Iterator var6 = var4.iterator();

        while (var6.hasNext()) {
            ResolveInfo var7 = (ResolveInfo) var6.next();
            String var8 = var7.activityInfo.packageName;
            if (var8.contains("com.tencent.mtt")) {
                var5 = true;
                break;
            }
        }

        return var5;
    }

    private static Bundle a(Context var0, Map<String, String> var1) {
        try {
            if (var1 == null) {
                return null;
            } else {
                Bundle var2 = new Bundle();
                var2.putString("style", var1.get("style") == null ? "0" : (String) var1.get("style"));

                try {
                    int var3 = Color.parseColor((String) var1.get("topBarBgColor"));
                    var2.putInt("topBarBgColor", var3);
                } catch (Exception var12) {
                    ;
                }

                if (var1 != null && var1.containsKey("menuData")) {
                    String var14 = (String) var1.get("menuData");
                    JSONObject var4 = null;
                    var4 = new JSONObject(var14);
                    JSONArray var5 = var4.getJSONArray("menuItems");
                    if (var5 != null) {
                        ArrayList var6 = new ArrayList();

                        for (int var7 = 0; var7 < var5.length() && var7 < 5; ++var7) {
                            try {
                                JSONObject var8 = (JSONObject) var5.get(var7);
                                int var9 = var8.getInt("iconResId");
                                Bitmap var10 = BitmapFactory.decodeResource(var0.getResources(), var9);
                                var6.add(var7, var10);
                                var8.put("iconResId", var7);
                            } catch (Exception var11) {
                                ;
                            }
                        }

                        var2.putParcelableArrayList("resArray", var6);
                    }

                    var2.putString("menuData", var4.toString());
                }

                return var2;
            }
        } catch (Exception var13) {
            var13.printStackTrace();
            return null;
        }
    }

    public static void openFileReaderListWithQBDownload(Context var0, String var1, ValueCallback<String> var2) {
        if (var0 != null && !var0.getApplicationInfo().packageName.equals("com.tencent.androidqqmail")) {
            String var3 = "选择其它应用打开";
            Intent var4 = new Intent("android.intent.action.VIEW");
            var4.addCategory("android.intent.category.DEFAULT");
            String var5 = com.tencent.smtt.sdk.b.a.i.c(var1);
            var4.setDataAndType(fromFile(var0, new File(var1)), var5);
            var4.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            QbSdk.isDefaultDialog = false;
            f var6 = new f(var0, var3, var4, var2, var5);
            String var7 = var6.a();
            if (var7 != null && !TextUtils.isEmpty(var7) && checkApkExist(var0, var7)) {
                if ("com.tencent.mtt".equals(var7)) {
                    var4.putExtra("ChannelID", var0.getApplicationContext().getPackageName());
                    var4.putExtra("PosID", "4");
                }

                var4.setPackage(var7);
                var0.startActivity(var4);
                if (var2 != null) {
                    var2.onReceiveValue("default browser:" + var7);
                }
            } else {
                if ("com.tencent.rtxlite".equalsIgnoreCase(var0.getApplicationContext().getPackageName()) && QbSdk.isDefaultDialog) {
                    return;
                }

                if (QbSdk.isDefaultDialog) {
                    if (var2 != null) {
                        var2.onReceiveValue("can not open");
                    }
                } else {
                    var6.show();
                }
            }

        }
    }

    public static boolean checkApkExist(Context var0, String var1) {
        if (var1 != null && !"".equals(var1)) {
            try {
                // ApplicationInfo var2 = var0.getPackageManager().getApplicationInfo(var1, 8192);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    ApplicationInfo var2 = var0.getPackageManager().getApplicationInfo(var1, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                } else {
                    ApplicationInfo var2 = var0.getPackageManager().getApplicationInfo(var1, PackageManager.GET_UNINSTALLED_PACKAGES);
                }
                return true;
            } catch (PackageManager.NameNotFoundException var3) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean a(Context var0, String var1, int var2, String var3, HashMap<String, String> var4, Bundle var5) {
        try {
            Intent var6 = new Intent("com.tencent.QQBrowser.action.sdk.document");
            if (var4 != null) {
                Set var7 = var4.keySet();
                if (var7 != null) {
                    Iterator var8 = var7.iterator();

                    while (var8.hasNext()) {
                        String var9 = (String) var8.next();
                        String var10 = (String) var4.get(var9);
                        if (!TextUtils.isEmpty(var10)) {
                            var6.putExtra(var9, var10);
                        }
                    }
                }
            }

            File var12 = new File(var1);
            var6.putExtra("key_reader_sdk_id", 3);
            var6.putExtra("key_reader_sdk_type", var2);
            if (var2 == 0) {
                var6.putExtra("key_reader_sdk_path", var1);
            } else if (var2 == 1) {
                var6.putExtra("key_reader_sdk_url", var1);
            }

            var6.putExtra("key_reader_sdk_format", var3);
            var6.setDataAndType(fromFile(var0, var12), "mtt/" + var3);
            var6.putExtra("loginType", d(var0.getApplicationContext()));
            if (var5 != null) {
                var6.putExtra("key_reader_sdk_extrals", var5);
            }

            var0.startActivity(var6);
            return true;
        } catch (Exception var11) {
            var11.printStackTrace();
            return false;
        }
    }

    private static int d(Context var0) {
        byte var1 = 26;
        String var2 = var0.getApplicationInfo().processName;
        if (var2.equals("com.tencent.mobileqq")) {
            var1 = 13;
        } else if (var2.equals("com.qzone")) {
            var1 = 14;
        } else if (var2.equals("com.tencent.WBlog")) {
            var1 = 15;
        } else if (var2.equals("com.tencent.mm")) {
            var1 = 24;
        }

        return var1;
    }

    /**
     * 创建
     * @param context
     * @param file
     * @return
     */
    private static Uri fromFile(Context context, @NonNull File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, TbsConstant.FILE_PROVIDER, file);
        } else {
            return Uri.fromFile(file);
        }
    }
}
