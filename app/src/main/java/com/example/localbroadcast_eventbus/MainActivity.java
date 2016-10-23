package com.example.localbroadcast_eventbus;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_EVENTS = 10000;
    private int buttonId = 0;
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver(MAX_EVENTS);
    static final String ACTION_ASYNC = "MyBroadcastAction.Async";
    static final String ACTION_SYNC = "MyBroadcastAction.Sync";
    static final String EXTRA_TIMESTAMP = "EXTRA_TIMESTAMP";
    private EventReceiver eventReceiver = new EventReceiver(MAX_EVENTS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpButtons();
    }

    private void setUpButtons() {
        findViewById(R.id.button_localbroadcast_async).setOnClickListener(v -> {
            sendBroadcasts(v, ACTION_ASYNC);
        });
        findViewById(R.id.button_localbroadcast_sync).setOnClickListener(v -> {
            sendBroadcasts(v, ACTION_SYNC);
        });
        findViewById(R.id.button_eventbus_posting).setOnClickListener(v -> {
            MyEvent myEvent = new MyEvent.PostingThread(System.nanoTime());
            postEvents(myEvent, v);
        });
        findViewById(R.id.button_eventbus_main).setOnClickListener(v -> {
            MyEvent myEvent = new MyEvent.MainThread(System.nanoTime());
            postEvents(myEvent, v);
        });
        findViewById(R.id.button_eventbus_background).setOnClickListener(v -> {
            MyEvent myEvent = new MyEvent.BackgroundThread(System.nanoTime());
            postEvents(myEvent, v);
        });
        findViewById(R.id.button_eventbus_async).setOnClickListener(v -> {
            MyEvent myEvent = new MyEvent.AsyncThread(System.nanoTime());
            postEvents(myEvent, v);
        });
    }

    private void sendBroadcasts(View v, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_TIMESTAMP, System.nanoTime());
        myBroadcastReceiver.resetCount();
        buttonId = v.getId();

        for (int i = 0; i < MAX_EVENTS; ++i) {
            LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
        }
    }

    private void postEvents(MyEvent event, View button) {
        buttonId = button.getId();
        eventReceiver.resetCount();
        for (int i = 0; i < MAX_EVENTS; ++i) {
            EventBus.getDefault().post(event);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateResult(Results results) {

        String str = getResources().getString(R.string.elapsed, results.elapsedNs / 1000000.0, results.averageNs / 1000000.0);
        TextView resultsTextView;

        switch (buttonId) {
            case R.id.button_localbroadcast_async:
                resultsTextView = (TextView) findViewById(R.id.results_localbroadcast_async);
                break;
            case R.id.button_localbroadcast_sync:
                resultsTextView = (TextView) findViewById(R.id.results_localbroadcast_sync);
                break;
            case R.id.button_eventbus_posting:
                resultsTextView = (TextView) findViewById(R.id.results_eventbus_posting);
                break;
            case R.id.button_eventbus_main:
                resultsTextView = (TextView) findViewById(R.id.results_eventbus_main);
                break;
            case R.id.button_eventbus_background:
                resultsTextView = (TextView) findViewById(R.id.results_eventbus_background);
                break;
            case R.id.button_eventbus_async:
                resultsTextView = (TextView) findViewById(R.id.results_eventbus_async);
                break;
            default:
                throw new IllegalStateException("You have a normal feeling for a moment, then it passes.");
        }

        resultsTextView.setText(str);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(eventReceiver);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(myBroadcastReceiver, new IntentFilter(ACTION_ASYNC));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(myBroadcastReceiver, new IntentFilter(ACTION_SYNC));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcastReceiver);
        EventBus.getDefault().unregister(eventReceiver);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
