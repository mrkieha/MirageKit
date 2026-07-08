package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.MirageValidation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lightweight server-tick task scheduler.
 *
 * @since 1.0
 */
public class MirageScheduler {

    private final List<Task> tasks = new ArrayList<>();
    private long globalTick = 0L;

    /**
     * Schedules a one-shot task.
     *
     * @param delayTicks ticks to wait
     * @param action     the runnable
     */
    public void runLater(int delayTicks, Runnable action) {
        MirageValidation.requireNonNull(action, "action");
        tasks.add(new Task(globalTick + Math.max(0, delayTicks), 0, action, null));
    }

    /**
     * Schedules a repeating task.
     *
     * @param delayTicks  initial delay
     * @param periodTicks interval between runs
     * @param action      the runnable
     */
    public void runRepeating(int delayTicks, int periodTicks, Runnable action) {
        MirageValidation.requireNonNull(action, "action");
        tasks.add(new Task(globalTick + Math.max(0, delayTicks), Math.max(1, periodTicks), action, null));
    }

    /**
     * Schedules a self-cancelling repeating task.
     *
     * @param delayTicks  initial delay
     * @param periodTicks interval between runs
     * @param action      consumer receiving the task handle
     */
    public void runTimer(int delayTicks, int periodTicks, Consumer<Task> action) {
        MirageValidation.requireNonNull(action, "action");
        Task task = new Task(globalTick + Math.max(0, delayTicks), Math.max(1, periodTicks), null, action);
        tasks.add(task);
    }

    /** Advances by one server tick. Must be called once per tick. */
    public void tick() {
        globalTick++;
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (task.cancelled) {
                it.remove();
                continue;
            }
            if (globalTick >= task.nextRun) {
                if (task.period == 0) {
                    task.run();
                    it.remove();
                } else {
                    task.run();
                    task.nextRun += task.period;
                }
            }
        }
    }

    /** @return the number of pending tasks */
    public int pendingTasks() {
        return tasks.size();
    }

    /** Clears all tasks without executing them. */
    public void clear() {
        tasks.clear();
    }

    /**
     * Task handle for self-cancelling timers.
     */
    public static final class Task {
        private long nextRun;
        private final long period;
        private final Runnable runnable;
        private final Consumer<Task> consumer;
        private boolean cancelled = false;

        private Task(long nextRun, long period, Runnable runnable, Consumer<Task> consumer) {
            this.nextRun   = nextRun;
            this.period    = period;
            this.runnable  = runnable;
            this.consumer  = consumer;
        }

        private void run() {
            if (runnable != null) runnable.run();
            if (consumer != null) consumer.accept(this);
        }

        /** Cancels future executions. */
        public void cancel() {
            this.cancelled = true;
        }

        /** @return true if cancelled */
        public boolean isCancelled() {
            return cancelled;
        }

        /** @return the tick at which this task will run next */
        public long getNextRun() {
            return nextRun;
        }

        /** @return the repeat period in ticks, or 0 for one-shot tasks */
        public long getPeriod() {
            return period;
        }
    }
}