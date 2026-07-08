package dev.mrkieha.mirage.util;

import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Node in a unified transformation hierarchy.
 *
 * <p>Each node stores a <b>local</b> transform relative to its parent.
 * The <b>world</b> transform is computed lazily (cached) and invalidated
 * automatically when the local transform or parent world transform changes.</p>
 *
 * <p>Supports arbitrary nesting: nodes can be parents, children, grandchildren.
 * The hierarchy is a directed tree (no cycles are checked — avoid them).</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * TransformNode root = new TransformNode("root");
 * root.localTransform(MirageTransform.identity().translate(10, 64, 10));
 *
 * TransformNode child = new TransformNode("hand");
 * child.setParent(root);
 * child.localTransform(MirageTransform.identity().translate(0, 1, 0).rotateX(45));
 *
 * Vec3d worldPos = child.worldPosition(); // computed from root + local
 * }</pre>
 *
 * @since 1.0
 */
public class TransformNode {

    private final String name;
    private TransformNode parent;
    private final List<TransformNode> children = new ArrayList<>();

    private MirageTransform localTransform = MirageTransform.identity();
    private Matrix4d cachedWorldMatrix = null;
    private boolean dirty = true;

    /**
     * Creates a root node with the given debug name.
     *
     * @param name identifier shown in toString / debug output
     */
    public TransformNode(String name) {
        this.name = name;
    }

    /* ---------- Hierarchy ---------- */

    /**
     * Attaches this node as a child of {@code newParent}.
     *
     * @param newParent parent node; {@code null} to detach
     */
    public void setParent(TransformNode newParent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }
        this.parent = newParent;
        if (newParent != null) {
            newParent.children.add(this);
        }
        markDirty();
    }

    /** @return parent node or {@code null} if this node is a root */
    public TransformNode getParent() {
        return parent;
    }

    /** @return unmodifiable view of children */
    public List<TransformNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /** @return {@code true} if this node has no parent */
    public boolean isRoot() {
        return parent == null;
    }

    /** @return {@code true} if this node has no children */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /* ---------- Local Transform ---------- */

    /**
     * Replaces the local transform (relative to parent).
     *
     * @param transform new local transform
     */
    public void localTransform(MirageTransform transform) {
        this.localTransform = MirageValidation.requireNonNull(transform, "transform");
        markDirty();
    }

    /** @return the current local transform (mutable reference — mutate with care) */
    public MirageTransform localTransform() {
        return localTransform;
    }

    /* ---------- World Transform (cached) ---------- */

    /**
     * Computes the world transformation matrix by recursively combining
     * {@code parent.worldMatrix() × localMatrix}.
     *
     * @return 4×4 affine world matrix
     */
    public Matrix4d worldMatrix() {
        if (!dirty && cachedWorldMatrix != null) {
            return cachedWorldMatrix;
        }

        Matrix4d local = localTransform.toMatrix4d();

        if (parent == null) {
            cachedWorldMatrix = local;
        } else {
            cachedWorldMatrix = new Matrix4d(parent.worldMatrix()).mul(local);
        }

        dirty = false;
        return cachedWorldMatrix;
    }

    /**
     * Extracts world-space position from the world matrix.
     *
     * @return world position
     */
    public Vec3d worldPosition() {
        Matrix4d m = worldMatrix();
        return new Vec3d(m.m30(), m.m31(), m.m32());
    }

    /**
     * Extracts world-space rotation quaternion.
     *
     * @return world rotation
     */
    public Quaterniond worldRotation() {
        Matrix4d m = worldMatrix();
        Quaterniond q = new Quaterniond();
        m.getUnnormalizedRotation(q);
        return q;
    }

    /**
     * Extracts world-space scale.
     *
     * @return world scale vector
     */
    public Vector3d worldScale() {
        Matrix4d m = worldMatrix();
        double sx = Math.sqrt(m.m00() * m.m00() + m.m01() * m.m01() + m.m02() * m.m02());
        double sy = Math.sqrt(m.m10() * m.m10() + m.m11() * m.m11() + m.m12() * m.m12());
        double sz = Math.sqrt(m.m20() * m.m20() + m.m21() * m.m21() + m.m22() * m.m22());
        return new Vector3d(sx, sy, sz);
    }

    /* ---------- Invalidation ---------- */

    /**
     * Marks this node and <b>all descendants</b> dirty so that their world
     * matrices are recomputed on next access.
     */
    public void markDirty() {
        this.dirty = true;
        this.cachedWorldMatrix = null;
        for (TransformNode child : children) {
            child.markDirty();
        }
    }

    /** @return {@code true} if world matrix needs recomputation */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Transforms a point from local space to world space.
     *
     * @param localPoint point in local coordinates
     * @return world-space point
     */
    public Vec3d transformPoint(Vec3d localPoint) {
        Matrix4d m = worldMatrix();
        Vector4d v = new Vector4d(localPoint.x, localPoint.y, localPoint.z, 1.0);
        m.transform(v);
        return new Vec3d(v.x, v.y, v.z);
    }

    /**
     * Transforms a point from world space to local space.
     *
     * @param worldPoint point in world coordinates
     * @return local-space point
     */
    public Vec3d inverseTransformPoint(Vec3d worldPoint) {
        Matrix4d inv = new Matrix4d(worldMatrix()).invert();
        Vector4d v = new Vector4d(worldPoint.x, worldPoint.y, worldPoint.z, 1.0);
        inv.transform(v);
        return new Vec3d(v.x, v.y, v.z);
    }

    /** @return node name (for debugging) */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TransformNode{" + name + "}";
    }
}