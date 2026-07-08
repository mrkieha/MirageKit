package dev.mrkieha.mirage.util;

import net.minecraft.util.math.AffineTransformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Fluent builder for {@link AffineTransformation}.
 *
 * <p>Encapsulates translation, scale, dual-quaternion rotation (left + right),
 * and pivot point. All mutating methods return {@code this} for chaining. The class is intentionally
 * <b>not</b> thread-safe; create one instance per logical transform.</p>
 *
 * <p><b>Rotation semantics:</b></p>
 * <ul>
 *   <li>{@code rotateX/Y/Z/Axis} — <b>relative</b>: accumulates on top of existing rotation.</li>
 *   <li>{@code rotationX/Y/Z/Axis} — <b>absolute</b>: replaces the entire left rotation.</li>
 *   <li>{@code rotateLocalX/Y/Z} — mutates the quaternion in-place around local axes.</li>
 * </ul>
 *
 * <p><b>Pivot note:</b> the pivot is fully supported in {@link #toMatrix4f()} and
 * {@link #toMatrix4d()} used by the transform hierarchy. When calling {@link #build()},
 * the pivot is currently ignored because vanilla {@link AffineTransformation} does not
 * natively store it; the entity transform will not rotate around the pivot.</p>
 *
 * @since 1.0
 */
public class MirageTransform {

    private Vector3f translation = new Vector3f(0f, 0f, 0f);
    private Vector3f scale       = new Vector3f(1f, 1f, 1f);
    private Quaternionf leftRot  = new Quaternionf();
    private Quaternionf rightRot = new Quaternionf();
    private Vector3f pivot       = new Vector3f(0f, 0f, 0f);

    private MirageTransform() {}

    /** Creates an identity transform (no translation, unit scale, zero rotation). */
    public static MirageTransform identity() {
        return new MirageTransform();
    }

    /**
     * Deep-copies another transform.
     *
     * @param other source transform; must not be {@code null}
     * @return independent copy
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public static MirageTransform of(MirageTransform other) {
        MirageValidation.requireNonNull(other, "other");
        MirageTransform t = new MirageTransform();
        t.translation.set(other.translation);
        t.scale.set(other.scale);
        t.leftRot.set(other.leftRot);
        t.rightRot.set(other.rightRot);
        t.pivot.set(other.pivot);
        return t;
    }

    /**
     * Creates a {@code MirageTransform} from a vanilla {@link AffineTransformation}.
     *
     * @param vanilla the vanilla transformation to decompose
     * @return a new MirageTransform representing the same state
     */
    public static MirageTransform fromVanilla(AffineTransformation vanilla) {
        MirageValidation.requireNonNull(vanilla, "vanilla");
        MirageTransform t = new MirageTransform();
        t.translation.set(vanilla.getTranslation());
        t.scale.set(vanilla.getScale());
        t.leftRot.set(vanilla.getLeftRotation());
        t.rightRot.set(vanilla.getRightRotation());
        return t;
    }

    /* ---------- Translation ---------- */

    /** Sets absolute translation from components. */
    public MirageTransform translate(float x, float y, float z) {
        this.translation.set(x, y, z);
        return this;
    }

    /** Sets absolute translation from a vector (copied internally). */
    public MirageTransform translate(Vector3f vec) {
        this.translation.set(vec);
        return this;
    }

    /** Adds an offset to the current translation. */
    public MirageTransform translateRelative(float x, float y, float z) {
        this.translation.add(x, y, z);
        return this;
    }

    /* ---------- Scale ---------- */

    /** Sets non-uniform scale. */
    public MirageTransform scale(float x, float y, float z) {
        this.scale.set(x, y, z);
        return this;
    }

    /** Sets uniform scale on all axes. */
    public MirageTransform scale(float uniform) {
        return scale(uniform, uniform, uniform);
    }

    /** Multiplies current scale component-wise. */
    public MirageTransform scaleRelative(float x, float y, float z) {
        this.scale.mul(x, y, z);
        return this;
    }

    /* ---------- Pivot ---------- */

    /**
     * Sets the pivot point around which rotations and scaling occur.
     *
     * <p>Applied in {@link #toMatrix4f()} / {@link #toMatrix4d()}.
     * Ignored by {@link #build()} because vanilla {@link AffineTransformation}
     * does not store pivot.</p>
     */
    public MirageTransform pivot(float x, float y, float z) {
        this.pivot.set(x, y, z);
        return this;
    }

    /** Sets the pivot point from a vector (copied internally). */
    public MirageTransform pivot(Vector3f vec) {
        this.pivot.set(vec);
        return this;
    }

    /* ---------- Absolute rotations (replace) ---------- */

    /** Replaces the left rotation with a rotation around the X axis. */
    public MirageTransform rotationX(float degrees) {
        this.leftRot = new Quaternionf().rotateX((float) Math.toRadians(degrees));
        return this;
    }

    /** Replaces the left rotation with a rotation around the Y axis. */
    public MirageTransform rotationY(float degrees) {
        this.leftRot = new Quaternionf().rotateY((float) Math.toRadians(degrees));
        return this;
    }

    /** Replaces the left rotation with a rotation around the Z axis. */
    public MirageTransform rotationZ(float degrees) {
        this.leftRot = new Quaternionf().rotateZ((float) Math.toRadians(degrees));
        return this;
    }

    /** Replaces the left rotation with a rotation around an arbitrary axis. */
    public MirageTransform rotationAxis(float degrees, float x, float y, float z) {
        this.leftRot = new Quaternionf().rotateAxis((float) Math.toRadians(degrees), x, y, z);
        return this;
    }

    /* ---------- Relative rotations (accumulate) ---------- */

    /** Accumulates a rotation around the X axis on top of the existing left rotation. */
    public MirageTransform rotateX(float degrees) {
        float rad = (float) Math.toRadians(degrees);
        new Quaternionf().rotateX(rad).mul(this.leftRot, this.leftRot);
        return this;
    }

    /** Accumulates a rotation around the Y axis on top of the existing left rotation. */
    public MirageTransform rotateY(float degrees) {
        float rad = (float) Math.toRadians(degrees);
        new Quaternionf().rotateY(rad).mul(this.leftRot, this.leftRot);
        return this;
    }

    /** Accumulates a rotation around the Z axis on top of the existing left rotation. */
    public MirageTransform rotateZ(float degrees) {
        float rad = (float) Math.toRadians(degrees);
        new Quaternionf().rotateZ(rad).mul(this.leftRot, this.leftRot);
        return this;
    }

    /** Accumulates a rotation around an arbitrary axis. */
    public MirageTransform rotateAxis(float degrees, float x, float y, float z) {
        float rad = (float) Math.toRadians(degrees);
        new Quaternionf().rotateAxis(rad, x, y, z).mul(this.leftRot, this.leftRot);
        return this;
    }

    /** Accumulates rotations around all three axes in XYZ order. */
    public MirageTransform rotate(float x, float y, float z) {
        return rotateX(x).rotateY(y).rotateZ(z);
    }

    /* ---------- Local rotations (mutate in-place) ---------- */

    /** Rotates the existing quaternion in-place around its local X axis. */
    public MirageTransform rotateLocalX(float degrees) {
        this.leftRot.rotateX((float) Math.toRadians(degrees));
        return this;
    }

    /** Rotates the existing quaternion in-place around its local Y axis. */
    public MirageTransform rotateLocalY(float degrees) {
        this.leftRot.rotateY((float) Math.toRadians(degrees));
        return this;
    }

    /** Rotates the existing quaternion in-place around its local Z axis. */
    public MirageTransform rotateLocalZ(float degrees) {
        this.leftRot.rotateZ((float) Math.toRadians(degrees));
        return this;
    }

    /* ---------- Raw quaternion setters ---------- */

    /** Replaces the left rotation quaternion outright (copied internally). */
    public MirageTransform leftRotation(Quaternionf q) {
        this.leftRot.set(q);
        return this;
    }

    /** Replaces the right rotation quaternion outright (copied internally). */
    public MirageTransform rightRotation(Quaternionf q) {
        this.rightRot.set(q);
        return this;
    }

    /* ---------- Utility ---------- */

    /** Resets all components to identity. */
    public MirageTransform reset() {
        this.translation.set(0f, 0f, 0f);
        this.scale.set(1f, 1f, 1f);
        this.leftRot.identity();
        this.rightRot.identity();
        this.pivot.set(0f, 0f, 0f);
        return this;
    }

    /** @return {@code true} if translation, scale and both rotations are at default values */
    public boolean isIdentity() {
        return translation.equals(0f, 0f, 0f)
                && scale.equals(1f, 1f, 1f)
                && leftRot.equals(0f, 0f, 0f, 1f)
                && rightRot.equals(0f, 0f, 0f, 1f)
                && pivot.equals(0f, 0f, 0f);
    }

    /**
     * Linearly interpolates between this transform and a target.
     *
     * <p>Pivot is <b>not</b> interpolated; the result inherits this transform's pivot.</p>
     *
     * @param target destination transform
     * @param t      interpolation factor in [0, 1]; clamped automatically
     * @return a new independent transform representing the intermediate state
     */
    public MirageTransform lerp(MirageTransform target, float t) {
        MirageValidation.requireNonNull(target, "target");
        t = MirageMath.clamp01(t);

        Vector3f tVec = new Vector3f(translation).lerp(target.translation, t);
        Vector3f sVec = new Vector3f(scale).lerp(target.scale, t);
        Quaternionf lRot = new Quaternionf(leftRot).slerp(target.leftRot, t);
        Quaternionf rRot = new Quaternionf(rightRot).slerp(target.rightRot, t);

        return MirageTransform.identity()
                .translate(tVec)
                .scale(sVec.x, sVec.y, sVec.z)
                .leftRotation(lRot)
                .rightRotation(rRot)
                .pivot(this.pivot);
    }

    /**
     * Builds a vanilla {@link AffineTransformation}.
     *
     * <p><b>Warning:</b> the pivot point is ignored in the returned transformation
     * because vanilla does not support it. Use {@link #toMatrix4f()} for full pivot
     * support inside custom transform pipelines (e.g. hierarchies).</p>
     *
     * @return immutable vanilla transformation ready for entity application
     */
    public AffineTransformation build() {
        return new AffineTransformation(translation, leftRot, scale, rightRot);
    }

    /**
     * Returns a JOML {@link Matrix4f} representing this transform, including pivot.
     *
     * <p>The resulting matrix applies translation, pivot offset, rotation,
     * scale, and inverse pivot offset in the correct order.</p>
     *
     * @return a new matrix containing the full transform
     */
    public Matrix4f toMatrix4f() {
        Matrix4f m = new Matrix4f();
        Vector3f p = getPivot();

        m.translate(translation.x, translation.y, translation.z);
        if (!p.equals(0f, 0f, 0f)) {
            m.translate(p.x, p.y, p.z);
        }
        m.rotate(leftRot);
        m.scale(scale.x, scale.y, scale.z);
        if (!rightRot.equals(0f, 0f, 0f, 1f)) {
            m.rotate(rightRot);
        }
        if (!p.equals(0f, 0f, 0f)) {
            m.translate(-p.x, -p.y, -p.z);
        }
        return m;
    }

    /**
     * Returns a JOML {@link org.joml.Matrix4d} representing this transform, including pivot.
     *
     * @return a new matrix containing the full transform
     */
    public org.joml.Matrix4d toMatrix4d() {
        org.joml.Matrix4d m = new org.joml.Matrix4d();
        Vector3f p = getPivot();

        m.translate(translation.x, translation.y, translation.z);
        if (!p.equals(0f, 0f, 0f)) {
            m.translate(p.x, p.y, p.z);
        }
        m.rotate(new org.joml.Quaterniond(leftRot.x, leftRot.y, leftRot.z, leftRot.w));
        m.scale(scale.x, scale.y, scale.z);
        if (!rightRot.equals(0f, 0f, 0f, 1f)) {
            m.rotate(new org.joml.Quaterniond(rightRot.x, rightRot.y, rightRot.z, rightRot.w));
        }
        if (!p.equals(0f, 0f, 0f)) {
            m.translate(-p.x, -p.y, -p.z);
        }
        return m;
    }

    /* ---------- Getters ---------- */

    /** @return a copy of the current translation vector */
    public Vector3f getTranslation() { return new Vector3f(translation); }

    /** @return a copy of the current scale vector */
    public Vector3f getScale()       { return new Vector3f(scale); }

    /** @return a copy of the current left rotation quaternion */
    public Quaternionf getLeftRot()  { return new Quaternionf(leftRot); }

    /** @return a copy of the current right rotation quaternion */
    public Quaternionf getRightRot() { return new Quaternionf(rightRot); }

    /** @return a copy of the current pivot vector */
    public Vector3f getPivot()       { return new Vector3f(pivot); }
}