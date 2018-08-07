package org.jetbrains.research.groups.ml_methods.refactoring.logging;

import com.google.gson.Gson;
import com.intellij.openapi.application.PermanentInstallationID;
import com.intellij.reporting.StatsSender;
import org.jetbrains.research.groups.ml_methods.utils.ArchitectureReloadedBundle;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RefactoringReporter {
    private BlockingQueue<String> messages = new LinkedBlockingQueue<>();
    private Gson gson = new Gson();
    private static final String REMOTE_LOGGING_PROPERTY = "enable.remote.logging";
    private Boolean enableLogging;

    public RefactoringReporter() {
        enableLogging = Boolean.valueOf(ArchitectureReloadedBundle.message(REMOTE_LOGGING_PROPERTY));

        if (enableLogging) {
            Thread uploaderThread = new Thread(new Uploader(messages));
            uploaderThread.start();
        }
    }

    public <T> void log(UUID session, T t) {
        if (!enableLogging)
            return;

        String builder = String.valueOf(System.currentTimeMillis()) +
                "\t" +
                "architecture-reloaded-plugin" +
                "\t" +
                1 +
                "\t" +
                PermanentInstallationID.get() +
                "\t" +
                session +
                "\t" +
                "refactor" +
                "\t" +
                "-1" +
                "\t" +
                gson.toJson(t, t.getClass());
        messages.offer(builder);
    }

    private class Uploader implements Runnable {
        private StatsSender sender = StatsSender.INSTANCE;
        private BlockingQueue<String> queue;

        Uploader(BlockingQueue<String> queue)
        {
            this.queue = queue;
        }

        @Override
        public void run() {
            int errors = 0;
            final int MAX_ERRORS = 5;

            while (true) {
                try {
                    if (errors > MAX_ERRORS) {
                        Thread.sleep(10000 * errors);
                    }

                    String message = queue.take();
                    if (!sender.send(message, true)) {
                        errors++;
                        queue.put(message);
                    } else {
                        errors = 0;
                    }

                    System.out.println(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
