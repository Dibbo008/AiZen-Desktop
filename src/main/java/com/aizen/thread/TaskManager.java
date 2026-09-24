package com.aizen.thread;

import com.aizen.util.SceneManager;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Shared fixed thread pool (3 workers) used for every DB, PDF and network operation,
 * so the JavaFX Application Thread is never blocked.
 */
public final class TaskManager {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, runnable -> {
        Thread t = new Thread(runnable, "aizen-worker");
        t.setDaemon(true);
        return t;
    });

    private TaskManager() {
    }

    public static void submit(Task<?> task) {
        POOL.execute(task);
    }

    /**
     * Runs a task in the background, shows the progress indicator while it runs and delivers
     * the result / error back on the JavaFX Application Thread.
     */
    public static <T> void run(Task<T> task, ProgressIndicator indicator,
                               Consumer<T> onSuccess, Consumer<Throwable> onError) {
        if (indicator != null) {
            indicator.visibleProperty().bind(task.runningProperty());
        }
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        submit(task);
    }

    /** Same as above, with a default error handler that shows an alert dialog. */
    public static <T> void run(Task<T> task, ProgressIndicator indicator, Consumer<T> onSuccess) {
        run(task, indicator, onSuccess,
                ex -> SceneManager.error("Something went wrong", messageOf(ex)));
    }

    public static String messageOf(Throwable t) {
        if (t == null) {
            return "Unknown error";
        }
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }

    public static void shutdown() {
        POOL.shutdownNow();
    }
}
