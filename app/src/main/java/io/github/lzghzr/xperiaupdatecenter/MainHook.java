package io.github.lzghzr.xperiaupdatecenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
  private static final String appName = "XperiaUpdateCenter";

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
    if (lpparam.packageName.equals("com.sonyericsson.updatecenter")) {
      XposedHelpers.findAndHookMethod(
          "com.sonyericsson.updatecenter.uepclient.DeviceProperties",
          lpparam.classLoader,
          "getRootingStatus",
          Context.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              XposedBridge.log(appName + " getRootingStatus before: " + param.getResult());
              param.setResult("0");
              XposedBridge.log(appName + " getRootingStatus after: " + param.getResult());
            }
          });
      XposedHelpers.findAndHookMethod(
          "com.sonyericsson.updatecenter.uepclient.DeviceProperties",
          lpparam.classLoader,
          "getSonyProductCode",
          String.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              XposedBridge.log(appName + " getSonyProductCode before: " + param.getResult());
              XSharedPreferences xsp = new XSharedPreferences("io.github.lzghzr.xperiaupdatecenter", "code");
              String code = xsp.getString("code", "");
              boolean custom = xsp.getBoolean("custom", false);
              String customCode = xsp.getString("custom_code", "");
              if (custom) {
                param.setResult(customCode);
              } else if (!code.isEmpty()) {
                param.setResult(code);
              }
              XposedBridge.log(appName + " getSonyProductCode after: " + param.getResult());
            }
          });
      XposedHelpers.findAndHookMethod(
          "com.sonyericsson.updatecenter.ui.activity.AbstractDownloadActivity",
          lpparam.classLoader,
          "onCreateOptionsMenu",
          Menu.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              Activity activity = (Activity) param.thisObject;
              Menu menu = (Menu) param.args[0];
              MenuItem item = menu.add("Product Code");
              item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(@NonNull MenuItem item) {
                  Intent intent = new Intent(Intent.ACTION_MAIN);
                  intent.setClassName(
                      "io.github.lzghzr.xperiaupdatecenter",
                      "io.github.lzghzr.xperiaupdatecenter.SettingsActivity"
                  );
                  activity.startActivity(intent);
                  return true;
                }
              });
            }
          });
    }
  }
}
