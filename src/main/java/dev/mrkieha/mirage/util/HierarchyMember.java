package dev.mrkieha.mirage.util;

import dev.mrkieha.mirage.MirageHierarchy;
import net.minecraft.util.math.Vec3d;

/**
 * Minimal contract for objects that can be manipulated by {@link MirageHierarchy}.
 *
 * <p>All three operations are expected to run synchronously on the server thread.</p>
 *
 * @since 1.0
 */
public interface HierarchyMember {

    /**
     * Teleports or repositions this member to the given world-space coordinates.
     *
     * @param pos destination
     * @return this member for chaining
     */
    HierarchyMember moveTo(Vec3d pos);

    /**
     * Enables vanilla interpolation for the next transform change.
     *
     * @param durationTicks how many ticks the transition should last
     * @return this member for chaining
     */
    HierarchyMember interpolateNow(int durationTicks);

    /**
     * Removes this member from the world permanently.
     */
    void remove();
}
