package org.a0x00sec.amunet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

public class TimerService extends Service {

    ScreenStatusMonitor screenStatusMonitor;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("0x00sec", "Service started.");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(TimerService.this, ServerUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                60000,
                pendingIntent);

        // Create an IntentFilter instance.
        IntentFilter intentFilter = new IntentFilter();

        // Add network connectivity change action.
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");

        // Set broadcast receiver priority.
        intentFilter.setPriority(100);

        screenStatusMonitor = new ScreenStatusMonitor();

        registerReceiver(screenStatusMonitor, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("0x00sec", "Service stop.");
        unregisterReceiver(screenStatusMonitor);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ScreenStatusMonitor extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(Intent.ACTION_SCREEN_OFF.equals(action)) {

                Log.d("0x00sec", "Screen is turn off.");

            } else if(Intent.ACTION_SCREEN_ON.equals(action)) {

                Log.d("0x00sec", "Screen is turn on.");

            }

        }
    }
}
