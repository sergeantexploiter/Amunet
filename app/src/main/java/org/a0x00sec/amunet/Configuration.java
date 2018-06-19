package org.a0x00sec.amunet;

public class Configuration {

    private static final String app_host = "play.cardfinder.co";
    private static final String domain_path = "https://" + app_host + "/";
    private static final String app_auth = domain_path + "/auth.php";

    public static String getApp_host() {
        return app_host;
    }

    public static String getDomain_path() {
        return domain_path;
    }

    public static String getApp_auth() {
        return app_auth;
    }


}
