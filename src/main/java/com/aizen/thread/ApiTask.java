package com.aizen.thread;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;

/** Background task for AI / network calls. */
public class ApiTask<T> extends Task<T> {
    private final String label;
    private final Callable<T> work;

    public ApiTask(String label, Callable<T> work) {
        this.label = label;
        this.work = work;
    }

    @Override
    protected T call() throws Exception {
        updateMessage(label);
        updateProgress(-1, 1);
        return work.call();
    }
}
