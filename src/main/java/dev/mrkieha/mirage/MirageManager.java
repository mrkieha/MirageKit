package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.MirageTransform;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Tracks and queries {@link MirageDisplay} instances within a single world.
 *
 * @since 1.0
 */
public class MirageManager {

    private final Map<UUID, MirageDisplay> tracked = new LinkedHashMap<>();
    private final ServerWorld world;

    /**
     * Creates a manager bound to a world.
     *
     * @param world the world
     */
    public MirageManager(ServerWorld world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    /**
     * Adds a display to tracking.
     *
     * @param display the display
     * @return the display
     */
    public MirageDisplay track(MirageDisplay display) {
        tracked.put(display.getUuid(), display);
        return display;
    }

    /**
     * Bulk-tracks displays.
     *
     * @param displays the displays
     * @return this
     */
    public MirageManager trackAll(MirageDisplay... displays) {
        for (MirageDisplay d : displays) track(d);
        return this;
    }

    /**
     * Tracks a display only if absent.
     *
     * @param display the display
     * @return true if newly tracked
     */
    public boolean trackIfAbsent(MirageDisplay display) {
        return tracked.putIfAbsent(display.getUuid(), display) == null;
    }

    /**
     * Gets a display by UUID.
     *
     * @param uuid the UUID
     * @return optional containing the display
     */
    public Optional<MirageDisplay> get(UUID uuid) {
        return Optional.ofNullable(tracked.get(uuid));
    }

    /**
     * Gets a display by network ID.
     *
     * @param entityId the entity ID
     * @return optional containing the display
     */
    public Optional<MirageDisplay> getById(int entityId) {
        return tracked.values().stream()
                .filter(d -> d.getEntityId() == entityId)
                .findFirst();
    }

    /**
     * Finds the first display matching a predicate.
     *
     * @param predicate the filter
     * @return optional containing the first match
     */
    public Optional<MirageDisplay> findFirst(Predicate<MirageDisplay> predicate) {
        return tracked.values().stream().filter(predicate).findFirst();
    }

    /**
     * Checks if a UUID is tracked.
     *
     * @param uuid the UUID
     * @return true if tracked
     */
    public boolean isTracked(UUID uuid) {
        return tracked.containsKey(uuid);
    }

    /**
     * Removes and discards a display.
     *
     * @param uuid the UUID
     * @return true if removed
     */
    public boolean remove(UUID uuid) {
        MirageDisplay display = tracked.remove(uuid);
        if (display != null) {
            display.remove();
            return true;
        }
        return false;
    }

    /** Removes and discards all tracked displays. */
    public void removeAll() {
        tracked.values().forEach(MirageDisplay::remove);
        tracked.clear();
    }

    /**
     * Removes dead entities from tracking.
     *
     * @return number of entries removed
     */
    public int cleanup() {
        int before = tracked.size();
        tracked.entrySet().removeIf(e -> !e.getValue().isAlive());
        return before - tracked.size();
    }

    /**
     * Iterates over all tracked displays.
     *
     * @param action the consumer
     */
    public void forEach(Consumer<MirageDisplay> action) {
        tracked.values().forEach(action);
    }

    /**
     * Returns all displays matching a predicate.
     *
     * @param predicate the filter
     * @return list of matches
     */
    public List<MirageDisplay> filter(Predicate<MirageDisplay> predicate) {
        return tracked.values().stream().filter(predicate).toList();
    }

    /**
     * Counts matching displays.
     *
     * @param predicate the filter
     * @return the count
     */
    public long count(Predicate<MirageDisplay> predicate) {
        return tracked.values().stream().filter(predicate).count();
    }

    /**
     * Applies a transform to all tracked displays.
     *
     * @param transform     the transform
     * @param durationTicks interpolation duration
     */
    public void animateAll(MirageTransform transform, int durationTicks) {
        tracked.values().forEach(d -> d
                .transform(transform)
                .interpolation(durationTicks)
                .startInterpolation(0)
        );
    }

    /**
     * Runs a custom action on all tracked displays.
     *
     * @param action the consumer
     */
    public void animateAll(Consumer<MirageDisplay> action) {
        tracked.values().forEach(action);
    }

    /**
     * Finds all displays inside a box.
     *
     * @param box the search volume
     * @return list of wrappers
     */
    public List<MirageDisplay> findInBox(Box box) {
        List<MirageDisplay> result = new ArrayList<>();
        world.getEntitiesByClass(DisplayEntity.class, box, e -> true)
                .forEach(e -> {
                    UUID id = e.getUuid();
                    MirageDisplay existing = tracked.get(id);
                    if (existing != null && existing.isAlive()) {
                        result.add(existing);
                    } else {
                        result.add(new MirageDisplay(e, world));
                    }
                });
        return result;
    }

    /**
     * Finds all displays near a point.
     *
     * @param center the center
     * @param radius the radius in blocks
     * @return list of wrappers
     */
    public List<MirageDisplay> findNear(Vec3d center, double radius) {
        Box box = new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
        return findInBox(box);
    }

    /** @return an unmodifiable view of all tracked displays */
    public Collection<MirageDisplay> getAll() {
        return Collections.unmodifiableCollection(tracked.values());
    }

    /** @return the number of tracked displays */
    public int size() {
        return tracked.size();
    }

    /** @return true if empty */
    public boolean isEmpty() {
        return tracked.isEmpty();
    }

    /** @return the world */
    public ServerWorld getWorld() {
        return world;
    }
}
