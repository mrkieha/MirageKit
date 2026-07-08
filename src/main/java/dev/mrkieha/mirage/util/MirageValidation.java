package dev.mrkieha.mirage.util;

/**
 * Lightweight precondition helpers that fail fast on invalid input.
 *
 * <p>Every method throws either {@link IllegalArgumentException} or
 * {@link NullPointerException} immediately on violation, surfacing bugs
 * during development rather than in production.</p>
 *
 * @since 1.0
 */
public final class MirageValidation {
    private MirageValidation() {}

    /**
     * Ensures the supplied reference is not {@code null}.
     *
     * @param obj     the reference to check
     * @param message detail message for the exception if the check fails
     * @param <T>     the type of the reference
     * @return the validated non-null reference
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) throw new NullPointerException(message);
        return obj;
    }

    /**
     * Ensures a value is strictly positive ({@code > 0}).
     *
     * @param value   the value to check
     * @param message detail message for the exception
     * @return the validated value
     * @throws IllegalArgumentException if {@code value} is zero or negative
     */
    public static int requirePositive(int value, String message) {
        if (value <= 0) throw new IllegalArgumentException(message);
        return value;
    }

    /**
     * Ensures a value lies inside the closed range [{@code min}, {@code max}].
     *
     * @param value   the value to check
     * @param min     inclusive lower bound
     * @param max     inclusive upper bound
     * @param message detail message for the exception
     * @return the validated value
     * @throws IllegalArgumentException if {@code value} is outside the range
     */
    public static int requireRange(int value, int min, int max, String message) {
        if (value < min || value > max) throw new IllegalArgumentException(message);
        return value;
    }

    /**
     * Ensures a floating-point value is finite and non-negative.
     *
     * @param value   the value to check
     * @param message detail message for the exception
     * @return the validated value
     * @throws IllegalArgumentException if {@code value} is negative, NaN or infinite
     */
    public static float requireNonNegative(float value, String message) {
        if (Float.isNaN(value) || Float.isInfinite(value) || value < 0f)
            throw new IllegalArgumentException(message);
        return value;
    }
}
