package dev.mrkieha.mirage.util;

import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

/**
 * Pure mathematical utilities for spatial calculations.
 *
 * <p>All methods are side-effect free. Hot-path overloads that accept
 * pre-allocated output objects are provided where allocation pressure matters.</p>
 *
 * @since 1.0
 */
public final class MirageMath {
    private MirageMath() {}

    /**
     * Linearly interpolates between two scalars.
     *
     * @param start value at {@code t = 0}
     * @param end   value at {@code t = 1}
     * @param t     interpolation factor, automatically clamped to [0, 1]
     * @return interpolated value
     */
    public static float lerp(float start, float end, float t) {
        t = clamp01(t);
        return start + (end - start) * t;
    }

    /**
     * Linearly interpolates between two {@code double} scalars.
     *
     * @param start value at {@code t = 0}
     * @param end   value at {@code t = 1}
     * @param t     interpolation factor, clamped to [0, 1]
     * @return interpolated value
     */
    public static double lerp(double start, double end, float t) {
        t = clamp01(t);
        return start + (end - start) * t;
    }

    /**
     * Spherical linear interpolation between two quaternions.
     *
     * @param a    start rotation
     * @param b    end rotation
     * @param t    interpolation factor clamped to [0, 1]
     * @param dest quaternion into which the result is written; may be reused
     * @return {@code dest} for chaining
     */
    public static Quaternionf slerp(Quaternionf a, Quaternionf b, float t, Quaternionf dest) {
        t = clamp01(t);
        a.slerp(b, t, dest);
        return dest;
    }

    /**
     * Euclidean distance between two points in 3-D space.
     *
     * @param a first point
     * @param b second point
     * @return distance in blocks; always non-negative
     */
    public static double distance(Vec3d a, Vec3d b) {
        return Math.sqrt(a.squaredDistanceTo(b));
    }

    /**
     * Calculates the yaw angle (horizontal rotation) required for an entity
     * at {@code from} to face {@code to}.
     *
     * @param from source position
     * @param to   target position
     * @return yaw in degrees, Minecraft convention (0 = south, 90 = west)
     */
    public static float yawTowards(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /**
     * Calculates the pitch angle (vertical rotation) required for an entity
     * at {@code from} to face {@code to}.
     *
     * @param from source position
     * @param to   target position
     * @return pitch in degrees, negative when looking upward
     */
    public static float pitchTowards(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return (float) Math.toDegrees(Math.atan2(-dy, horizontal));
    }

    /**
     * Creates a quaternion that rotates the +Z axis to point towards {@code target}.
     *
     * @param from   source position
     * @param target destination position
     * @return a new quaternion representing the look rotation
     */
    public static Quaternionf lookRotation(Vec3d from, Vec3d target) {
        float yaw = yawTowards(from, target);
        float pitch = pitchTowards(from, target);
        return new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw))
                .rotateX((float) Math.toRadians(pitch));
    }

    /**
     * Clamps a float to the inclusive range [0, 1].
     *
     * @param t value to clamp
     * @return 0 if {@code t < 0}, 1 if {@code t > 1}, otherwise {@code t}
     */
    public static float clamp01(float t) {
        return Math.max(0f, Math.min(1f, t));
    }

    /**
     * Clamps an {@code int} to the inclusive range [{@code min}, {@code max}].
     *
     * @param value input
     * @param min   lower bound
     * @param max   upper bound
     * @return clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
