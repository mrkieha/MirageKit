# Mirage

Server-side toolkit for Minecraft Fabric Display Entities.  
Provides a fluent API for building, animating, and managing display entity hierarchies without client-side mods.

---

## Features

- **Fluent Builder** — chainable `MirageBuilder` for configuring Block, Item and Text displays
- **Display Wrapper** — `MirageDisplay` abstracts `DisplayEntity` with transform, interpolation and lifecycle helpers
- **Keyframe Animation** — tick-driven `MirageAnimation` with hold / interpolate durations, looping and ping-pong
- **Transform Hierarchy** — `MirageHierarchy` with `TransformNode` trees, local / world space propagation
- **Display Groups** — `MirageGroup` for bulk transforms, distribution patterns and relative offsets
- **World Manager** — `MirageManager` tracks, queries and bulk-operates on displays per `ServerWorld`
- **Task Scheduler** — `MirageScheduler` for one-shot, repeating and self-cancelling tick tasks
- **Math Utilities** — `MirageTransform`, `MirageMath`, `MirageEasing` and `TransformNode` for spatial calculations

---

## Requirements

- Minecraft **1.20.1**
- Fabric Loader **≥ 0.14.0**
- Fabric API
- Java **17**

---

## Installation

Add JitPack to your `repositories` block:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

Add the dependency:

```groovy
dependencies {
    modImplementation 'com.github.mrkieha:Mirage:1.0.0'
}
```

---

## Quick Start

### 1. Creating a Display

```java
import dev.mrkieha.mirage.Mirage;
import dev.mrkieha.mirage.MirageDisplay;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

public void spawnSword(ServerWorld world) {
    MirageDisplay display = Mirage.builder(world)
        .item(Items.DIAMOND_SWORD.getDefaultStack())
        .at(100.5, 64.0, -50.5)
        .scale(2.0f)
        .rotateY(45f)
        .billboard(DisplayEntity.BillboardMode.CENTER)
        .glowColor(0xFF00FF)
        .fullBright()
        .buildAndSpawn();
}
```

### 2. Keyframe Animation

```java
import dev.mrkieha.mirage.Mirage;
import dev.mrkieha.mirage.MirageAnimation;
import dev.mrkieha.mirage.util.MirageTransform;

MirageAnimation anim = Mirage.animate(display)
    .keyframe(20, MirageTransform.identity().translate(5f, 2f, 0f))
    .keyframe(20, MirageTransform.identity().translate(0f, 0f, 0f))
    .loop(true)
    .play();

// The animation must be ticked. Register it via the scheduler:
Mirage.schedule().runRepeating(0, 1, anim::tick);
```

### 3. Transformation Hierarchy

```java
import dev.mrkieha.mirage.Mirage;
import dev.mrkieha.mirage.MirageHierarchy;
import net.minecraft.util.math.Vec3d;

MirageHierarchy<MirageDisplay> rig = Mirage.hierarchy("root");

// Add a child at a local offset
rig.add(display, new Vec3d(2.0, 0.0, 0.0));

// Move the whole rig in world space
rig.moveTo(new Vec3d(100.0, 64.0, 0.0));

// Rotate the root — children follow automatically
rig.rotateY(90.0);

// Sync interpolated
rig.syncDisplaysInterpolated(10);
```

### 4. Task Scheduler

```java
import dev.mrkieha.mirage.Mirage;

// One-shot
Mirage.schedule().runLater(20, () -> {
    display.glowColor(0x00FF00);
});

// Repeating every 10 ticks
Mirage.schedule().runRepeating(0, 10, () -> {
    display.interpolateNow(5).moveTo(display.getPos().add(0, 0.1, 0));
});

// Self-cancelling timer
Mirage.schedule().runTimer(0, 5, task -> {
    if (!display.isAlive()) task.cancel();
    display.offset(0, 0.1, 0);
});
```

### 5. Display Groups

```java
import dev.mrkieha.mirage.Mirage;
import dev.mrkieha.mirage.MirageGroup;

MirageGroup group = Mirage.group();
group.add(display1).add(display2).add(display3);

// Move together preserving offsets
group.moveTo(new Vec3d(0, 80, 0));

// Or interpolate
group.interpolateTo(new Vec3d(0, 80, 0), 20);

// Distribute in patterns
group.distributeCircle(new Vec3d(0, 64, 0), 5.0, 64.0);
group.distributeGrid(new Vec3d(0, 64, 0), 3, 2.5);

// Cleanup
group.removeAll();
```

### 6. World Manager

```java
import dev.mrkieha.mirage.Mirage;
import dev.mrkieha.mirage.MirageManager;

MirageManager manager = Mirage.of(world);
manager.track(display);

// Bulk operations
manager.animateAll(MirageTransform.identity().scale(0.5f), 10);
manager.forEach(d -> d.fullBright());

// Spatial queries
List<MirageDisplay> nearby = manager.findNear(player.getPos(), 32.0);

// Cleanup dead entities
manager.cleanup();
manager.removeAll();
```

---

## Architecture

```
dev.mrkieha.mirage
├── Mirage.java              — Static facade (builder, animate, group, hierarchy, schedule, of)
├── MirageBuilder.java       — Fluent factory for Block / Item / Text displays
├── MirageDisplay.java       — Wrapper around DisplayEntity; implements HierarchyMember
├── MirageAnimation.java     — Tick-driven keyframe animator
├── MirageHierarchy.java     — Parent-child transform tree for HierarchyMember instances
├── MirageGroup.java         — Logical collection with offsets and distribution helpers
├── MirageManager.java       — Per-world tracking, queries and bulk operations
├── MirageScheduler.java     — Server-tick task scheduler (one-shot, repeating, timer)
├── MirageMod.java           — Fabric ModInitializer; hooks scheduler into END_SERVER_TICK
└── util/
    ├── MirageTransform.java — Fluent builder for AffineTransformation (translation, rotation, scale, pivot)
    ├── TransformNode.java   — Cached local/world matrix node for hierarchies
    ├── HierarchyMember.java — Contract: moveTo, interpolateNow, remove
    ├── MirageMath.java      — lerp, slerp, distance, yawTowards, pitchTowards, lookRotation
    ├── MirageEasing.java    — linear, easeIn/Out Quad/Cubic, easeOutBack
    └── MirageValidation.java — Lightweight precondition helpers
```

---

## License

See [LICENSE](LICENSE.md) for details.
