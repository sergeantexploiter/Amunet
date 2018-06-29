package org.a0x00sec.amunet;

public class RecyclerJava {

    String permission_name;
    String[] permission_identifier;
    int permission_request_code;

    public String getPermission_name() {
        return permission_name;
    }

    public String[] getPermission_identifier() {
        return permission_identifier;
    }

    public int getPermission_request_code() {
        return permission_request_code;
    }

    public RecyclerJava(String permission_name, String[] permission_identifier, int permission_request_code) {
        this.permission_identifier = permission_identifier;
        this.permission_name = permission_name;
        this.permission_request_code = permission_request_code;
    }
}
