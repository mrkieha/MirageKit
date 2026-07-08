package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.MirageTransform;
import dev.mrkieha.mirage.util.MirageValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tick-driven keyframe animator for a single {@link MirageDisplay}.
 *
 * <p>Must be ticked manually, e.g. via {@link MirageScheduler}.</p>
 *
 * @since 1.0
 */
public class MirageAnimation {

    private final MirageDisplay display;
    private final List<Keyframe> keyframes = new ArrayList<>();

    private int currentKeyframe = 0;
    private int ticksInCurrent  = 0;
    private boolean loop        = false;
    private boolean pingPong  = false;
    private boolean running   = false;
    private float speed         = 1.0f;
    private float pendingTicks  = 0f;
    private int direction     = 1;

    private Consumer<MirageAnimation> onFinish = null;
    private Consumer<MirageAnimation> onStart  = null;
    private Consumer<MirageAnimation> onKeyframe = null;

    /**
     * Binds the animator to a display.
     *
     * @param display the target display
     */
    public MirageAnimation(MirageDisplay display) {
        this.display = MirageValidation.requireNonNull(display, "display");
    }

    /**
     * Adds a keyframe with equal hold and interpolation durations.
     *
     * @param holdTicks the hold duration in ticks
     * @param transform the target transform
     * @return this
     */
    public MirageAnimation keyframe(int holdTicks, MirageTransform transform) {
        keyframes.add(new Keyframe(holdTicks, transform));
        return this;
    }

    /**
     * Adds a keyframe with independent durations.
     *
     * @param holdTicks        the hold duration
     * @param interpolateTicks the interpolation duration
     * @param transform        the target transform
     * @return this
     */
    public MirageAnimation keyframe(int holdTicks, int interpolateTicks, MirageTransform transform) {
        keyframes.add(new Keyframe(holdTicks, interpolateTicks, transform));
        return this;
    }

    /**
     * Enables or disables looping.
     *
     * @param loop true to loop
     * @return this
     */
    public MirageAnimation loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Enables or disables ping-pong playback.
     *
     * @param pingPong true to ping-pong
     * @return this
     */
    public MirageAnimation pingPong(boolean pingPong) {
        this.pingPong = pingPong;
        return this;
    }

    /**
     * Sets playback speed.
     *
     * @param multiplier the speed multiplier
     * @return this
     */
    public MirageAnimation speed(float multiplier) {
        this.speed = Math.max(0f, multiplier);
        return this;
    }

    /**
     * Sets a callback invoked on finish.
     *
     * @param callback the callback
     * @return this
     */
    public MirageAnimation onFinish(Consumer<MirageAnimation> callback) {
        this.onFinish = callback;
        return this;
    }

    /**
     * Sets a callback invoked on start.
     *
     * @param callback the callback
     * @return this
     */
    public MirageAnimation onStart(Consumer<MirageAnimation> callback) {
        this.onStart = callback;
        return this;
    }

    /**
     * Sets a callback invoked on each keyframe advance.
     *
     * @param callback the callback
     * @return this
     */
    public MirageAnimation onKeyframe(Consumer<MirageAnimation> callback) {
        this.onKeyframe = callback;
        return this;
    }

    /** Starts or restarts playback from the first keyframe. */
    public MirageAnimation play() {
        if (keyframes.isEmpty()) return this;
        running         = true;
        currentKeyframe = 0;
        ticksInCurrent  = 0;
        direction       = 1;
        pendingTicks    = 0f;
        if (onStart != null) onStart.accept(this);
        applyCurrentKeyframe();
        return this;
    }

    /** Pauses playback. */
    public void pause() {
        running = false;
    }

    /** Resumes a paused animation. */
    public void resume() {
        running = true;
    }

    /** Stops and resets to the first keyframe. */
    public void stop() {
        running         = false;
        currentKeyframe = 0;
        ticksInCurrent  = 0;
        direction       = 1;
        pendingTicks    = 0f;
    }

    /** Reverses playback direction. */
    public MirageAnimation reverse() {
        this.direction *= -1;
        return this;
    }

    /**
     * Jumps to a keyframe.
     *
     * @param keyframeIndex the 0-based index
     * @return this
     */
    public MirageAnimation seek(int keyframeIndex) {
        if (keyframes.isEmpty()) return this;
        this.currentKeyframe = MirageValidation.requireRange(keyframeIndex, 0, keyframes.size() - 1, "keyframeIndex");
        this.ticksInCurrent = 0;
        applyCurrentKeyframe();
        return this;
    }

    /** @return true if running */
    public boolean isRunning() {
        return running;
    }

    /** @return true if paused */
    public boolean isPaused() {
        return !running && !keyframes.isEmpty();
    }

    /** @return the current keyframe index */
    public int getCurrentKeyframe() {
        return currentKeyframe;
    }

    /** @return the total keyframe count */
    public int getTotalKeyframes() {
        return keyframes.size();
    }

    /** @return progress in [0, 1) within the current keyframe */
    public float getProgress() {
        if (keyframes.isEmpty()) return 0f;
        Keyframe kf = keyframes.get(currentKeyframe);
        int required = Math.max(kf.holdTicks, kf.interpolateTicks);
        return required == 0 ? 0f : (float) ticksInCurrent / (float) required;
    }

    /**
     * Advances the animation by one tick.
     *
     * @return true while the animation is still running
     */
    public boolean tick() {
        if (!running || keyframes.isEmpty()) return false;
        if (!display.isAlive()) {
            running = false;
            return false;
        }

        pendingTicks += speed;
        if (pendingTicks < 1f) return running;

        int wholeTicks = (int) pendingTicks;
        pendingTicks -= wholeTicks;

        for (int i = 0; i < wholeTicks; i++) {
            Keyframe current = keyframes.get(currentKeyframe);
            ticksInCurrent++;

            int requiredTicks = Math.max(current.holdTicks, current.interpolateTicks);
            if (ticksInCurrent >= requiredTicks) {
                ticksInCurrent = 0;
                currentKeyframe += direction;

                if (currentKeyframe >= keyframes.size()) {
                    if (pingPong && keyframes.size() > 1) {
                        direction = -1;
                        currentKeyframe = keyframes.size() - 2;
                    } else if (loop) {
                        currentKeyframe = 0;
                    } else {
                        running = false;
                        if (onFinish != null) onFinish.accept(this);
                        return false;
                    }
                } else if (currentKeyframe < 0) {
                    if (pingPong && keyframes.size() > 1) {
                        direction = 1;
                        currentKeyframe = 1;
                    } else if (loop) {
                        currentKeyframe = keyframes.size() - 1;
                    } else {
                        running = false;
                        if (onFinish != null) onFinish.accept(this);
                        return false;
                    }
                }

                if (running) {
                    applyCurrentKeyframe();
                    if (onKeyframe != null) onKeyframe.accept(this);
                }
            }
        }

        return running;
    }

    private void applyCurrentKeyframe() {
        Keyframe kf = keyframes.get(currentKeyframe);
        display.transform(kf.transform)
                .interpolation(kf.interpolateTicks)
                .startInterpolation(0);
    }

    /**
     * Immutable keyframe descriptor.
     */
    public static final class Keyframe {
        public final int holdTicks;
        public final int interpolateTicks;
        public final MirageTransform transform;

        public Keyframe(int holdTicks, MirageTransform transform) {
            this(holdTicks, holdTicks, transform);
        }

        public Keyframe(int holdTicks, int interpolateTicks, MirageTransform transform) {
            this.holdTicks        = Math.max(1, holdTicks);
            this.interpolateTicks = Math.max(1, interpolateTicks);
            this.transform        = MirageValidation.requireNonNull(transform, "transform");
        }
    }
}