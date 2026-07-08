package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.HierarchyMember;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Static facade for the Mirage display toolkit.
 *
 * @since 1.0
 */
public final class Mirage {

    private static final Map<RegistryKey<World>, MirageManager> MANAGERS = new HashMap<>();
    private static final MirageScheduler SCHEDULER = new MirageScheduler();
    private static boolean tickHooked = false;

    private Mirage() {}

    /** Hooks the global scheduler into the server tick. Called by {@link MirageMod}. */
    static void hookTick() {
        if (tickHooked) return;
        tickHooked = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> SCHEDULER.tick());
    }

    /**
     * Returns or creates the {@link MirageManager} for the given world.
     *
     * @param world the server world
     * @return the world-scoped manager
     */
    public static MirageManager of(ServerWorld world) {
        return MANAGERS.computeIfAbsent(world.getRegistryKey(), k -> new MirageManager(world));
    }

    /**
     * Creates a {@link MirageBuilder} for the given world.
     *
     * @param world target world
     * @return a new builder
     */
    public static MirageBuilder builder(ServerWorld world) {
        return MirageBuilder.in(world);
    }

    /** @return the global scheduler */
    public static MirageScheduler schedule() {
        return SCHEDULER;
    }

    /**
     * Creates an animation for the given display.
     *
     * @param display the display to animate
     * @return a new animator
     */
    public static MirageAnimation animate(MirageDisplay display) {
        return new MirageAnimation(display);
    }

    /** @return a new empty group */
    public static MirageGroup group() {
        return new MirageGroup();
    }

    /**
     * Creates a hierarchy with the given root name.
     *
     * @param name root node name
     * @param <T>  member type
     * @return a new hierarchy
     */
    public static <T extends HierarchyMember> MirageHierarchy<T> hierarchy(String name) {
        return new MirageHierarchy<>(name);
    }
}
