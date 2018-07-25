package org.a0x00sec.amunet;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class Configuration {

    private static final String app_host = "10.10.1.107";
    private static final String domain_path = "http://" + app_host + "/";
    private static final String app_auth = domain_path + "/amunet/auth.php";

    public static String getApp_host() {
        return app_host;
    }

    public static String getDomain_path() {
        return domain_path;
    }

    public static String getApp_auth() {
        return app_auth;
    }

    protected String getDeviceIMEI(Context context) {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            try {
                deviceUniqueIdentifier = tm.getDeviceId();
            } catch (SecurityException e) {
                return null;
            }
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
    }
}
