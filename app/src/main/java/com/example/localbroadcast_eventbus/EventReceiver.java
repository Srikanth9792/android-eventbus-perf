package com.example.localbroadcast_eventbus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventReceiver {

    private int receivedEvents;
    private final int maxEvents;

    private static final int POSTING = 1;
    private static final int MAIN = 2;
    private static final int BACKGROUND = 3;
    private static final int ASYNC = 4;

    private Stats stats;

    EventReceiver(int maxEvents) {
        this.maxEvents = maxEvents;
        this.stats = new Stats();
    }

    void resetCount() {
        receivedEvents = 0;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)  // Default if ThreadMode not specified
    public void onEventPosting(MyEvent.PostingThread event) {
        postResultIfDone(event, POSTING);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(MyEvent.MainThread event) {
        postResultIfDone(event, MAIN);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackground(MyEvent.BackgroundThread event) {
        postResultIfDone(event, BACKGROUND);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(MyEvent.AsyncThread event) {
        postResultIfDone(event, ASYNC);
    }

    private void postResultIfDone(MyEvent event, int mode) {
        if (++receivedEvents == maxEvents) {
            long elapsedNs = System.nanoTime() - event.startNs;
            double averageNs = stats.getAvg(elapsedNs, mode);
            EventBus.getDefault().post(new Results(elapsedNs, averageNs));
        }
    }

}
