# MirageKit

[![](https://jitpack.io/v/mrkieha/MirageKit.svg)](https://jitpack.io/#mrkieha/MirageKit)

Server-side toolkit for Minecraft Fabric Display Entities.
Provides a fluent API for building, animating, and managing display entity hierarchies without client-side mods.

---

## Features

- **Fluent Builder** — chainable API for configuring display entities (transformation, interpolation, brightness, shadow, etc.)
- **Keyframe Animation System** — timeline-based animations with easing curves
- **Transformation Hierarchy** — parent-child relationships with local/world space transforms
- **Task Scheduler** — deferred and repeating tasks bound to entity lifecycle
- **Display Groups** — logical grouping with bulk operations
- **Math Utilities** — quaternion helpers, interpolation, matrix decomposition

---

## Requirements

- Minecraft **1.20.1**
- Fabric Loader **≥ 0.19.3**
- Fabric API **≥ 0.92.9**
- Java **17**

---

## Installation

### Gradle

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
    modImplementation 'com.github.mrkieha:MirageKit:1.0.0'
}
```

---

## Quick Start

### 1. Creating a Display Entity

```java
import dev.mrkieha.mirage.builder.DisplayEntityBuilder;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

public void spawnDisplay(ServerWorld world) {
    ItemDisplayEntity display = new DisplayEntityBuilder<ItemDisplayEntity>(world)
        .item(Items.DIAMOND_SWORD.getDefaultStack())
        .position(100.5, 64.0, -50.5)
        .scale(2.0f, 2.0f, 2.0f)
        .rotation(0f, 45f, 0f)
        .billboard(DisplayEntityBuilder.Billboard.CENTER)
        .glowColorOverride(0xFF00FF)
        .build();
}
```

### 2. Keyframe Animation

```java
import dev.mrkieha.mirage.animation.KeyframeAnimation;
import dev.mrkieha.mirage.animation.Easing;

KeyframeAnimation animation = KeyframeAnimation.builder()
    .duration(60) // ticks
    .keyframe(0, new Transform().position(0, 0, 0))
    .keyframe(30, new Transform().position(5, 2, 0), Easing.EASE_IN_OUT)
    .keyframe(60, new Transform().position(0, 0, 0), Easing.EASE_OUT)
    .loop(LoopMode.LOOP)
    .build();

animation.start(displayEntity);
```

### 3. Transformation Hierarchy

```java
import dev.mrkieha.mirage.hierarchy.DisplayNode;

DisplayNode root = new DisplayNode(parentDisplay);
DisplayNode child = new DisplayNode(childDisplay);

root.attach(child);
child.setLocalTransform(
    new Transform()
        .position(2.0, 0.0, 0.0)
        .rotation(0f, 90f, 0f)
);
// child automatically inherits parent's world transform
```

### 4. Task Scheduler

```java
import dev.mrkieha.mirage.scheduler.DisplayScheduler;

DisplayScheduler scheduler = DisplayScheduler.of(displayEntity);

scheduler.delay(20, () -> {
    displayEntity.setGlowColorOverride(0x00FF00);
});

scheduler.repeat(10, 5, () -> {
    // runs every 10 ticks, 5 times total
    displayEntity.setInterpolationDuration(5);
});
```

### 5. Display Groups

```java
import dev.mrkieha.mirage.group.DisplayGroup;

DisplayGroup group = new DisplayGroup("hologram");
group.add(display1, display2, display3);

group.setVisibility(false);
group.destroy(); // removes all entities
```

---

## Architecture

```
dev.mrkieha.mirage
├── builder/          — Fluent builders for all display entity types
├── animation/        — Keyframe engine, tweening, easing curves
├── hierarchy/        — Parent-child transform trees
├── scheduler/        — Tick-synchronized task execution
├── group/            — Entity collections and bulk ops
├── math/             — Quaternion, vector, matrix utilities
└── util/             — Helper methods for common operations
```

---

## License

See [LICENSE.md](LICENSE.md) for details.
