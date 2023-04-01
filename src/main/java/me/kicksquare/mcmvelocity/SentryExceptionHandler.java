package me.kicksquare.mcmvelocity;

import io.sentry.Sentry;
import me.kicksquare.mcmvelocity.util.LoggerUtil;

public class SentryExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        boolean isFromThisPlugin = false;
        for (StackTraceElement element : e.getStackTrace()) {
            //todo change this package name
            if (element.getClassName().contains("me.kicksquare.mcmvelocity")) {
                isFromThisPlugin = true;
                break;
            }
        }
        if(isFromThisPlugin) {
            LoggerUtil.severe("Detected an MCMetrics exception, uploading to sentry...");
            Sentry.captureException(e);
        }
    }
}
