package dev.mrkieha.mirage.util;

/**
 * Easing functions for non-linear interpolation.
 *
 * <p>Each method accepts a normalized time {@code t} in [0, 1] and returns
 * an eased value also in [0, 1]. Safe to call every tick; no allocations
 * occur on the hot path.</p>
 *
 * @since 1.0
 * @see MirageMath#clamp01(float)
 */
public final class MirageEasing {
    private MirageEasing() {}

    /** No easing; linear progression. */
    public static float linear(float t) {
        return MirageMath.clamp01(t);
    }

    /** Accelerating from zero velocity. */
    public static float easeInQuad(float t) {
        t = MirageMath.clamp01(t);
        return t * t;
    }

    /** Decelerating to zero velocity. */
    public static float easeOutQuad(float t) {
        t = MirageMath.clamp01(t);
        return 1f - (1f - t) * (1f - t);
    }

    /** Acceleration until halfway, then deceleration. */
    public static float easeInOutQuad(float t) {
        t = MirageMath.clamp01(t);
        return t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2f) / 2f;
    }

    /** Cubic ease-in. */
    public static float easeInCubic(float t) {
        t = MirageMath.clamp01(t);
        return t * t * t;
    }

    /** Cubic ease-out. */
    public static float easeOutCubic(float t) {
        t = MirageMath.clamp01(t);
        return 1f - (float) Math.pow(1f - t, 3f);
    }

    /** Cubic ease-in-out. */
    public static float easeInOutCubic(float t) {
        t = MirageMath.clamp01(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3f) / 2f;
    }

    /**
     * Slight overshoot before settling (ease-out back).
     *
     * <p>Great for UI pop-in effects or attention-grabbing spawn animations.</p>
     *
     * @param t normalized time
     * @return eased value; may briefly exceed 1 near the end
     */
    public static float easeOutBack(float t) {
        t = MirageMath.clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1f, 3f) + c1 * (float) Math.pow(t - 1f, 2f);
    }
}
