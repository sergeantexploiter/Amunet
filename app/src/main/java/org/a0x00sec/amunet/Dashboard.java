package org.a0x00sec.amunet;

import android.Manifest;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<RecyclerJava> recyclerJavaList = new ArrayList<>();
    private RecyclerAdapter recyclerAdapter;

    protected static final int GPS_REQUEST_CODE = 5000;
    protected static final int CONTACTS_REQUEST_CODE = 5001;
    protected static final int CALENDAR_REQUEST_CODE = 5002;
    protected static final int MIC_REQUEST_CODE = 5003;
    protected static final int CAMERA_REQUEST_CODE = 5004;
    protected static final int STORAGE_REQUEST_CODE = 5005;
    protected static final int SMS_REQUEST_CODE = 5006;
    protected static final int GET_ACCOUNTS_REQUEST_CODE = 5007;

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

        updateRecycler();
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
            }
            case SMS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "SMS Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
            case GET_ACCOUNTS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Phone Accounts Access Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "CAMERA Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
            case CALENDAR_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "CALENDAR Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
            case MIC_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "MIC Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
            case CONTACTS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "CONTACTS Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "STORAGE Permission Denied", Toast.LENGTH_LONG).show();
                }
                updateRecycler();
            }
        }
    }

    private void updateRecycler() {

        recyclerJavaList.clear();

        RecyclerJava sms_permission = new RecyclerJava("SMS",
                new String[] {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, SMS_REQUEST_CODE);
        recyclerJavaList.add(sms_permission);

        RecyclerJava accounts_permission = new RecyclerJava("ACCOUNTS",
                new String[] {Manifest.permission.GET_ACCOUNTS}, GET_ACCOUNTS_REQUEST_CODE);
        recyclerJavaList.add(accounts_permission);

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
                new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.PROCESS_OUTGOING_CALLS}, CONTACTS_REQUEST_CODE);
        recyclerJavaList.add(contact_permission);

        recyclerAdapter = new RecyclerAdapter(recyclerJavaList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.notifyDataSetChanged();
    }
}