package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.MirageMath;
import dev.mrkieha.mirage.util.MirageTransform;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Group of displays that preserves relative offsets from a center point.
 *
 * @since 1.0
 */
public class MirageGroup {

    private final List<MirageDisplay> displays = new ArrayList<>();
    private final Map<MirageDisplay, Vec3d> offsets = new HashMap<>();
    private Vec3d center = Vec3d.ZERO;

    /**
     * Sets the group center.
     *
     * @param center the center
     * @return this
     */
    public MirageGroup center(Vec3d center) {
        this.center = center;
        return this;
    }

    /**
     * Adds a display, snapping its current offset from center.
     *
     * @param display the display
     * @return this
     */
    public MirageGroup add(MirageDisplay display) {
        if (!displays.contains(display)) {
            displays.add(display);
            offsets.put(display, display.getPos().subtract(center));
        }
        return this;
    }

    /**
     * Adds a display with an explicit offset.
     *
     * @param display the display
     * @param offset  the offset from center
     * @return this
     */
    public MirageGroup add(MirageDisplay display, Vec3d offset) {
        if (!displays.contains(display)) {
            displays.add(display);
            offsets.put(display, offset);
        }
        return this;
    }

    /**
     * Bulk-adds displays.
     *
     * @param displays the collection
     * @return this
     */
    public MirageGroup addAll(Collection<MirageDisplay> displays) {
        displays.forEach(this::add);
        return this;
    }

    /**
     * Moves the group center.
     *
     * @param newCenter the target center
     */
    public void moveTo(Vec3d newCenter) {
        for (MirageDisplay d : displays) {
            Vec3d offset = offsets.getOrDefault(d, Vec3d.ZERO);
            d.moveTo(newCenter.add(offset));
        }
        this.center = newCenter;
    }

    /**
     * Moves the group center to coordinates.
     *
     * @param x world X
     * @param y world Y
     * @param z world Z
     */
    public void moveTo(double x, double y, double z) {
        moveTo(new Vec3d(x, y, z));
    }

    /**
     * Moves the group with interpolation.
     *
     * @param newCenter     the target center
     * @param durationTicks interpolation duration
     */
    public void interpolateTo(Vec3d newCenter, int durationTicks) {
        for (MirageDisplay d : displays) {
            Vec3d offset = offsets.getOrDefault(d, Vec3d.ZERO);
            d.moveTo(newCenter.add(offset), durationTicks);
        }
        this.center = newCenter;
    }

    /**
     * Applies a transform to all members instantly.
     *
     * @param transform the transform
     */
    public void transform(MirageTransform transform) {
        displays.forEach(d -> d.transform(transform).startInterpolation(0));
    }

    /**
     * Applies a transform with interpolation.
     *
     * @param transform     the transform
     * @param durationTicks interpolation duration
     */
    public void interpolate(MirageTransform transform, int durationTicks) {
        displays.forEach(d -> d.transform(transform).interpolation(durationTicks).startInterpolation(0));
    }

    /**
     * Distributes members evenly along a line.
     *
     * @param start the start point
     * @param end   the end point
     */
    public void distributeLine(Vec3d start, Vec3d end) {
        if (displays.isEmpty()) return;
        int n = displays.size();
        for (int i = 0; i < n; i++) {
            float t = n == 1 ? 0.5f : (float) i / (n - 1);
            Vec3d pos = new Vec3d(
                    MirageMath.lerp((float) start.x, (float) end.x, t),
                    MirageMath.lerp((float) start.y, (float) end.y, t),
                    MirageMath.lerp((float) start.z, (float) end.z, t)
            );
            MirageDisplay d = displays.get(i);
            d.moveTo(pos);
            offsets.put(d, pos.subtract(start.lerp(end, 0.5)));
        }
        this.center = start.lerp(end, 0.5);
    }

    /**
     * Distributes members evenly around a horizontal circle.
     *
     * @param center the circle center
     * @param radius the radius
     * @param y      the Y coordinate
     */
    public void distributeCircle(Vec3d center, double radius, double y) {
        if (displays.isEmpty()) return;
        int n = displays.size();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            Vec3d pos = new Vec3d(x, y, z);
            MirageDisplay d = displays.get(i);
            d.moveTo(pos);
            offsets.put(d, pos.subtract(center));
        }
        this.center = center;
    }

    /**
     * Arranges members in a flat grid.
     *
     * @param origin  the top-left corner
     * @param columns the number of columns
     * @param spacing the cell spacing
     */
    public void distributeGrid(Vec3d origin, int columns, double spacing) {
        if (displays.isEmpty() || columns <= 0) return;
        int n = displays.size();
        int rows = (int) Math.ceil((double) n / columns);
        Vec3d gridCenter = origin.add(
                (columns - 1) * spacing / 2.0,
                -(rows - 1) * spacing / 2.0,
                0
        );
        for (int i = 0; i < n; i++) {
            int row = i / columns;
            int col = i % columns;
            Vec3d pos = origin.add(col * spacing, -row * spacing, 0);
            MirageDisplay d = displays.get(i);
            d.moveTo(pos);
            offsets.put(d, pos.subtract(gridCenter));
        }
        this.center = gridCenter;
    }

    /** Removes all members from the world and clears the group. */
    public void removeAll() {
        displays.forEach(MirageDisplay::remove);
        displays.clear();
        offsets.clear();
    }

    /** Clears the group without removing entities. */
    public void clear() {
        displays.clear();
        offsets.clear();
    }

    /** @return unmodifiable view of members */
    public List<MirageDisplay> getDisplays() {
        return Collections.unmodifiableList(displays);
    }

    /** @return the current center */
    public Vec3d getCenter() {
        return center;
    }

    /** @return true if empty */
    public boolean isEmpty() {
        return displays.isEmpty();
    }

    /** @return the member count */
    public int size() {
        return displays.size();
    }
}