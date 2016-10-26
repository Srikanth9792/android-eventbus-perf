# android-eventbus-perf

## Description

A utility app demonstrating the difference in usage and performance between Android's LocalBroadcastManager and GreenRobot's [EventBus](http://greenrobot.org/eventbus/).

See the accompanying [blog post](http://www.codeflow.fi/2016/10/26/comparison-of-eventbus-and-androids-local-broadcasts/).

NOTE: This app explores only the performance of the EventBus in relation to local broadcasts, when sending multiple events/broadcasts in succession (i.e. in a for-loop). For single events/broadcasts send only sporadically, performance of the event/broadcast delivery itself is a non-issue. Furthermore, you can usually rearrange your event delivery to happen in bigger batches if performance, CPU and battery usage is an issue (which is basically always :) Therefore, you should not base your decision whether to use the EventBus library on this example code alone.
