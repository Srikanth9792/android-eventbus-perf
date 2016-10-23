package com.example.localbroadcast_eventbus;

public abstract class MyEvent {

    final long startNs;

    MyEvent(long timestamp) {
        this.startNs = timestamp;
    }

    static class PostingThread extends MyEvent {
        PostingThread(long timestamp) {
            super(timestamp);
        }
    }

    static class MainThread extends MyEvent {
        MainThread(long timestamp) {
            super(timestamp);
        }
    }

    static class BackgroundThread extends MyEvent {
        BackgroundThread(long timestamp) {
            super(timestamp);
        }
    }

    static class AsyncThread extends MyEvent {
        AsyncThread(long timestamp) {
            super(timestamp);
        }
    }

}
