package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.HierarchyMember;
import dev.mrkieha.mirage.util.TransformNode;
import dev.mrkieha.mirage.util.MirageTransform;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hierarchical group of {@link HierarchyMember} instances.
 *
 * <p>Transform changes on the root propagate to all descendants.
 * Supports nested hierarchies.</p>
 *
 * @since 1.0
 */
public class MirageHierarchy<T extends HierarchyMember> {

    private final TransformNode rootNode;
    private final List<Member<T>> members = new ArrayList<>();
    private int nextId = 0;

    /**
     * Creates a hierarchy with root at origin.
     *
     * @param name the root node name
     */
    public MirageHierarchy(String name) {
        this.rootNode = new TransformNode(name);
    }

    /**
     * Creates a hierarchy with root at the given position.
     *
     * @param name   the root node name
     * @param origin the initial position
     */
    public MirageHierarchy(String name, Vec3d origin) {
        this.rootNode = new TransformNode(name);
        this.rootNode.localTransform(MirageTransform.identity()
                .translate((float) origin.x, (float) origin.y, (float) origin.z));
    }

    /* ---------- Members ---------- */

    /**
     * Adds a member at a local offset.
     *
     * @param display     the member
     * @param localOffset the offset from root
     * @return the member handle
     */
    public Member<T> add(T display, Vec3d localOffset) {
        return add(display, localOffset, MirageTransform.identity());
    }

    /**
     * Adds a member with offset and local transform.
     *
     * <p>The offset replaces the translation component of {@code localTransform};
     * rotation, scale, pivot and right rotation are preserved.</p>
     *
     * @param display       the member
     * @param localOffset   the offset from root (replaces translation)
     * @param localTransform the local rotation, scale and pivot
     * @return the member handle
     */
    public Member<T> add(T display, Vec3d localOffset, MirageTransform localTransform) {
        TransformNode node = new TransformNode("member_" + (nextId++));
        node.setParent(rootNode);

        MirageTransform combined = MirageTransform.identity()
                .translate((float) localOffset.x, (float) localOffset.y, (float) localOffset.z)
                .leftRotation(localTransform.getLeftRot())
                .scale(localTransform.getScale().x, localTransform.getScale().y, localTransform.getScale().z)
                .rightRotation(localTransform.getRightRot())
                .pivot(localTransform.getPivot());

        node.localTransform(combined);
        Member<T> m = new Member<>(display, node);
        members.add(m);
        return m;
    }

    /**
     * Nests another hierarchy under this root.
     *
     * @param child  the child hierarchy
     * @param offset the local offset
     * @return the child root node
     */
    public TransformNode addHierarchy(MirageHierarchy<?> child, Vec3d offset) {
        child.rootNode.setParent(this.rootNode);
        MirageTransform current = child.rootNode.localTransform();
        child.rootNode.localTransform(MirageTransform.of(current)
                .translateRelative((float) offset.x, (float) offset.y, (float) offset.z));
        return child.rootNode;
    }

    /**
     * Detaches a member.
     *
     * @param member the member
     */
    public void remove(Member<T> member) {
        member.node.setParent(null);
        members.remove(member);
    }

    /** Detaches all members. */
    public void clear() {
        for (Member<T> m : members) {
            m.node.setParent(null);
        }
        members.clear();
    }

    /** Removes all members from the world and clears. */
    public void destroyAll() {
        for (Member<T> m : members) {
            m.display.remove();
            m.node.setParent(null);
        }
        members.clear();
    }

    /* ---------- Root transform ---------- */

    /**
     * Sets the root world position. Preserves rotation and scale.
     *
     * @param worldPos the position
     */
    public void moveTo(Vec3d worldPos) {
        MirageTransform current = rootNode.localTransform();
        rootNode.localTransform(MirageTransform.identity()
                .translate((float) worldPos.x, (float) worldPos.y, (float) worldPos.z)
                .leftRotation(current.getLeftRot())
                .scale(current.getScale().x, current.getScale().y, current.getScale().z)
                .rightRotation(current.getRightRot())
                .pivot(current.getPivot()));
        syncDisplays();
    }

    /**
     * Accumulates Y-axis rotation on the root.
     *
     * @param degrees angle in degrees
     */
    public void rotateY(double degrees) {
        rootNode.localTransform().rotateY((float) degrees);
        rootNode.markDirty();
        syncDisplays();
    }

    /**
     * Accumulates X-axis rotation on the root.
     *
     * @param degrees angle in degrees
     */
    public void rotateX(double degrees) {
        rootNode.localTransform().rotateX((float) degrees);
        rootNode.markDirty();
        syncDisplays();
    }

    /**
     * Accumulates Z-axis rotation on the root.
     *
     * @param degrees angle in degrees
     */
    public void rotateZ(double degrees) {
        rootNode.localTransform().rotateZ((float) degrees);
        rootNode.markDirty();
        syncDisplays();
    }

    /**
     * Accumulates uniform scale on the root.
     *
     * @param factor the scale multiplier
     */
    public void scale(double factor) {
        rootNode.localTransform().scaleRelative((float) factor, (float) factor, (float) factor);
        rootNode.markDirty();
        syncDisplays();
    }

    /**
     * Replaces the root local transform.
     *
     * @param transform the new transform
     */
    public void transform(MirageTransform transform) {
        rootNode.localTransform(transform);
        syncDisplays();
    }

    /* ---------- Sync ---------- */

    /** Pushes world positions to all displays instantly. */
    public void syncDisplays() {
        for (Member<T> m : members) {
            Vec3d worldPos = m.node.worldPosition();
            m.display.moveTo(worldPos);
        }
    }

    /** Pushes world positions with interpolation. */
    public void syncDisplaysInterpolated(int durationTicks) {
        for (Member<T> m : members) {
            Vec3d worldPos = m.node.worldPosition();
            m.display.interpolateNow(durationTicks);
            m.display.moveTo(worldPos);
        }
    }

    /* ---------- Queries ---------- */

    /** @return the root node */
    public TransformNode getRoot() {
        return rootNode;
    }

    /** @return unmodifiable view of members */
    public List<Member<T>> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /** @return the root world position */
    public Vec3d getCenter() {
        return rootNode.worldPosition();
    }

    /** @return the member count */
    public int size() {
        return members.size();
    }

    /** @return true if empty */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    /* ---------- Member handle ---------- */

    /**
     * Handle for a hierarchy member.
     */
    public static final class Member<T extends HierarchyMember> {
        private final T display;
        private final TransformNode node;

        Member(T display, TransformNode node) {
            this.display = display;
            this.node = node;
        }

        /** @return the display */
        public T display() { return display; }

        /** @return the transform node */
        public TransformNode node() { return node; }

        /** Replaces the member's local transform. */
        public void setLocalTransform(MirageTransform transform) {
            node.localTransform(transform);
        }

        /** @return the member's world position */
        public Vec3d worldPosition() {
            return node.worldPosition();
        }

        /** @return the member's world rotation */
        public org.joml.Quaterniond worldRotation() {
            return node.worldRotation();
        }
    }
}