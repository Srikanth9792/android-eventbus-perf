package com.example.localbroadcast_eventbus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

class MyBroadcastReceiver extends BroadcastReceiver {

    private int receivedBroadcasts = 0;
    private final int maxEvents;

    private static final int ASYNC = 1;
    private static final int SYNC = 2;

    private Stats stats;

    public MyBroadcastReceiver(int maxEvents) {
        this.maxEvents = maxEvents;
        this.stats = new Stats();
    }

    public void resetCount() {
        receivedBroadcasts = 0;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (++receivedBroadcasts == maxEvents) {

            switch (intent.getAction()) {
                case MainActivity.ACTION_ASYNC:
                    postResult(intent, ASYNC);
                    break;
                case MainActivity.ACTION_SYNC:
                    postResult(intent, SYNC);
                    break;
            }
        }
    }

    private void postResult(Intent intent, int mode) {
        long startNs = intent.getLongExtra(MainActivity.EXTRA_TIMESTAMP, -1);
        long elapsedNs = System.nanoTime() - startNs;
        double averageNs = stats.getAvg(elapsedNs, mode);

        EventBus.getDefault().post(new Results(elapsedNs, averageNs));
    }

}
