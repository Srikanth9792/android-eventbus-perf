# android-eventbus-perf

### Description

A utility app demonstrating the difference in performance between Android's LocalBroadcastManager and GreenRobot's [EventBus](http://greenrobot.org/eventbus/).

NOTE: This app explores only the performance of the EventBus in relation to local broadcasts, when sending multiple events/broadcasts in succession (i.e. in a for-loop). For single events/broadcasts send only sporadically, performance of the event/broadcast delivery itself is a non-issue. Furthermore, you can usually rearrange your event delivery to happen in bigger batches if performance, CPU and battery usage is an issue (which is basically always :) Therefore, you should not base your decision whether to use the EventBus library on this example code alone.

## Comparing EventBus and LocalBroadcastManager

EventBus has a number of benefits compared to local broadcasts. Note that discussion here only concerns broadcasts sent using LocalBroadcastManager, i.e. broadcasts that are sent and received only within the same process. System-wide broadcasts sent using `Context.sendBroadcast()` are not considered here.

### Event delivery on specific threads

You can select whether to receive EventBus events on different threads based on threadMode setting you (optionally) specify in the `@Subscribe` annotation (the following is adapted from EventBus [documentation](http://greenrobot.org/eventbus/documentation/delivery-threads-threadmode/) ):

- `@Subscribe(threadMode = ThreadMode.POSTING)` The event receiver is called on the same thread that posted the event. Note that the event callback is called synchronously, i.e. on the current iteration of the run loop. This is the default setting if you leave out the `threadMode` setting.
- `@Subscribe(threadMode = ThreadMode.MAIN)` The event callback is executed on the main/UI thread of the process. This is especially useful e.g. when you want to update your views based on work that has been completed in the background. As always, it is important not to stall the main thread by executing long-lasting operation in the callback. If you call post the event from the main thread, a callback using this `threadMode` is executed directly.
- `@Subscribe(threadMode = ThreadMode.BACKGROUND)` Calls the event receiver method on a dedicated worker thread, unless the posting thread is not the main thread, in which case the callback is executed directly. If there are multiple callbacks incoming, each of them is executed sequentially. Keep this in mind when handling the event -- it may not be feasible to perform any heavy-lifting operations while other callbacks may be waiting to be handled.
- `@Subscribe(threadMode = ThreadMode.ASYNC)` This option is useful when you need to handle multiple callbacks concurrently. The calling thread is one from a separate ThreadPool of worker threads, and never the posting thread. Remember to be careful with this in your event handler, since multiple threads may be executing the code asynchronously.

By contrast, BroadcastReceiver's onReceive() is always called on the process' main thread, unless you call `sendBroadcastSync()` from some other thread which, as the named implies, is executed directly.

### Callback registration

To register callbacks that you want EventBus to call when certain types of event objects are posted, you specify the posted object type as parameter to your callback methods. You also need to register the object of the class where your callbacks are implemented. This is roughly equivalent to registering a broadcast receiver with a set of intent filters, as shown in this example:

```
public class MyActivity extends Activity {

    // ...
    
    private MyReceiver myReceiver = new MyReceiver();
    private static final BROADCAST_ACTION = "BROADCAST_ACTION";

    @Override
    public void onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(myReceiver, new IntentFilter(BROADCAST_ACTION));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onStop();
    }

    @Subscribe
    public void onEvent(MyEvent event) {
        // Do stuff with received event ...
    }

    private static class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Do stuff with received broadcast ...
        }

    }

}
```

As you can see, writing a broadcast receiver is somewhat more cumbersome since you need to extend the BroadcastReceiver class and supply an intent filter, whereas you can just annotate your event handling methods with `@Subscribe` when using EventBus.

### Posting events and broadcasts

This is an area where LocalBroadcastManager and EventBus are very similar to each other, without much boilerplate even with LocalBroadcastManager:

```
public void postEvent() {
    MyEvent event = new MyEvent();
    EventBus.getDefault().post(event);
}

public void sendBroadcast() {
    Intent intent = new Intent("my_action");
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
}
```

### Delivering content

With broadcasts you need to package your payload using intent extras, which can often require quite a bit of boilerplate code, especially when using [Parcelables](https://developer.android.com/reference/android/os/Parcelable.html). With EventBus you can place your content within the event object itself. This requires no cumbersome packaging, no parcelables, etc. The content of the event you post is delivered as-is to the callback handler. Let's expand the methods in the previous section to supply some payload as well:

```
class Event {
    int value;
    String text;
    MyObject myObject;
    
    public Event(int value, String text, MyObject myObject) {
        this.value = value;
        this.text = text;
        this.myObject = myObject;
    }
}

public void postEvent() {
    MyEvent event = new MyEvent(123, "abcdef", new MyObject());
    EventBus.getDefault().post(event);
}

public void sendBroadcast() {
    Intent intent = new Intent("my_action");
    intent.putExtra("int_extra", 123);
    intent.putExtra("text_extra", "abcdef);
    intent.putExtra("myobject_extra", new MyObject());   // <- MyObject needs to be a Parcelable
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
}
```
Passing the example instance of MyObject to a broadcast intent is not as simple as it looks, since the MyObject class needs to implement the Parcelable interface as specified in Android documentation. (Note that you should really use constant definitions for names of extras, they're literals above only for conciseness and clarity.)

With EventBus, you can use the posted event directly in your callback handler, whereas with broadcasts you first need to unpack the payload from extras in your broadcast receiver:

```
@Subscribe
public void onEvent(MyEvent event) {
    Log.d(TAG, "value: " + event.value);
    Log.d(TAG, "text: " + event.text);
    Log.d(TAG, "myObject.doStuff(): " + event.myObject.doStuff());
}

// ...

private static class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int value = intent.getIntExtra("int_extra", -1);
        String text = intent.getStringExtra("text_extra");
        MyObject myObject = (MyObject) intent.getParcelable("myobject_extra");
        Log.d(TAG, "value: " + value);
        Log.d(TAG, "text: " + text);
        Log.d(TAG, "myObject.doStuff(): " + myObject.doStuff());
    }

}
```

### Conclusion

EventBus is a great library for decoupling your application architecture, delivering results on specific threads, and having to write no unnecessary boilerplate code when handling event payloads. While very convenient compared to simple local broadcasts, EventBus' tradeoff comes in the lack of performance, although it's still quite performant for most tasks. All things considered, for most intents and purposes EventBus is a very good choice due to its versatility.
