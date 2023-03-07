package me.kicksquare.mcmvelocity;

import io.sentry.Sentry;

public class SentryExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // loop through the stack trace and print it out
        boolean isFromThisPlugin = false;
        for (StackTraceElement element : e.getStackTrace()) {
            //todo change this package name
            if (element.getClassName().contains("me.kicksquare.mcmvelocity")) {
                isFromThisPlugin = true;
                break;
            }
        }
        if(isFromThisPlugin) {
            System.out.println("Detected an MCMetrics exception. Uploading to sentry.");
            Sentry.captureException(e);
        }
    }
}
