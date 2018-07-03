package org.a0x00sec.amunet;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<RecyclerJava> recyclerJavaList = new ArrayList<>();
    private RecyclerAdapter recyclerAdapter;

    private Button service_monitor_btn;

    protected static final int GPS_REQUEST_CODE = 5000;
    protected static final int CONTACTS_REQUEST_CODE = 5001;
    protected static final int CALENDAR_REQUEST_CODE = 5002;
    protected static final int MIC_REQUEST_CODE = 5003;
    protected static final int CAMERA_REQUEST_CODE = 5004;
    protected static final int STORAGE_REQUEST_CODE = 5005;
    protected static final int SMS_REQUEST_CODE = 5006;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.dashboard_toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.dashboard_recycler_view);

        recyclerAdapter = new RecyclerAdapter(recyclerJavaList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(Dashboard.this, LinearLayoutManager.VERTICAL));

        service_monitor_btn = findViewById(R.id.service_monitor_button);

        if(MyServiceIsRunning(TimerService.class)) {
            service_monitor_btn.setText("STOP MONITORING");
        } else {
            service_monitor_btn.setText("START MONITORING");
        }

        service_monitor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MyServiceIsRunning(TimerService.class)) {
                    Log.i("0x00sec", "Stopping Service ...");
                    stopService(new Intent(Dashboard.this, TimerService.class));
                    service_monitor_btn.setText("START MONITORING");
                } else {
                    Log.i("0x00sec", "Starting Service ...");
                    startService(new Intent(Dashboard.this, TimerService.class));
                    service_monitor_btn.setText("STOP MONITORING");
                }
            }
        });

        updateRecycler();
    }

    private boolean MyServiceIsRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void PermissionRequestHandler(String[] permission_identifier, int RequestCode) {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), permission_identifier[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Dashboard.this, permission_identifier, RequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GPS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "GPS Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
            case SMS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "SMS Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "CAMERA Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
            case CALENDAR_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "CALENDAR Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
            case MIC_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "MIC Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
            case CONTACTS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "CONTACTS Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "STORAGE Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
                break;
            }
        }
    }

    private void updateRecycler() {

        recyclerJavaList.clear();

        RecyclerJava sms_permission = new RecyclerJava("SMS",
                new String[] {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, SMS_REQUEST_CODE);
        recyclerJavaList.add(sms_permission);

        RecyclerJava camera_permission = new RecyclerJava("Camera",
                new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        recyclerJavaList.add(camera_permission);

        RecyclerJava filesystem_permission = new RecyclerJava("Storage",
                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        recyclerJavaList.add(filesystem_permission);

        RecyclerJava calendar_permission = new RecyclerJava("Calendar",
                new String[] {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, CALENDAR_REQUEST_CODE);
        recyclerJavaList.add(calendar_permission);

        RecyclerJava gps_permission = new RecyclerJava("GPS Location",
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_REQUEST_CODE);
        recyclerJavaList.add(gps_permission);

        RecyclerJava mic_permission = new RecyclerJava("Record Microphone",
                new String[] {Manifest.permission.RECORD_AUDIO}, MIC_REQUEST_CODE);
        recyclerJavaList.add(mic_permission);

        RecyclerJava contact_permission = new RecyclerJava("Contacts & Call Logs",
                new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG}, CONTACTS_REQUEST_CODE);
        recyclerJavaList.add(contact_permission);

        recyclerAdapter = new RecyclerAdapter(recyclerJavaList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }
}