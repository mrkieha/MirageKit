package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.HierarchyMember;
import dev.mrkieha.mirage.util.MirageMath;
import dev.mrkieha.mirage.util.MirageTransform;
import dev.mrkieha.mirage.util.MirageValidation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side wrapper around {@link DisplayEntity}.
 *
 * <p>All mutation must happen on the server thread.</p>
 *
 * @since 1.0
 */
public class MirageDisplay implements HierarchyMember {

    private final DisplayEntity entity;
    private final ServerWorld world;
    private final Map<String, Object> userData = new HashMap<>();

    MirageDisplay(DisplayEntity entity, ServerWorld world) {
        this.entity = MirageValidation.requireNonNull(entity, "entity");
        this.world  = MirageValidation.requireNonNull(world, "world");
    }

    /* ---------- Content ---------- */

    /**
     * Sets the block state. No-op if not a block display.
     *
     * @param state the block state
     * @return this
     */
    public MirageDisplay block(BlockState state) {
        if (entity instanceof DisplayEntity.BlockDisplayEntity block) {
            block.setBlockState(state);
        }
        return this;
    }

    /**
     * @return the block state, or {@code null} if this is not a block display
     */
    public @Nullable BlockState getBlock() {
        if (entity instanceof DisplayEntity.BlockDisplayEntity block) {
            return block.getBlockState();
        }
        return null;
    }

    /**
     * Sets the item stack. No-op if not an item display.
     *
     * @param stack the item stack
     * @return this
     */
    public MirageDisplay item(ItemStack stack) {
        if (entity instanceof DisplayEntity.ItemDisplayEntity item) {
            item.setItemStack(stack);
        }
        return this;
    }

    /**
     * @return the item stack, or {@link ItemStack#EMPTY} if this is not an item display
     */
    public ItemStack getItem() {
        if (entity instanceof DisplayEntity.ItemDisplayEntity item) {
            return item.getItemStack();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Sets the text. No-op if not a text display.
     *
     * @param text the text
     * @return this
     */
    public MirageDisplay text(Text text) {
        if (entity instanceof DisplayEntity.TextDisplayEntity txt) {
            txt.setText(text);
        }
        return this;
    }

    /**
     * @return the text, or {@code null} if this is not a text display
     */
    public @Nullable Text getText() {
        if (entity instanceof DisplayEntity.TextDisplayEntity txt) {
            return txt.getText();
        }
        return null;
    }

    /* ---------- Transform ---------- */

    /**
     * Applies a transform.
     *
     * @param transform the transform
     * @return this
     */
    public MirageDisplay transform(MirageTransform transform) {
        entity.setTransformation(MirageValidation.requireNonNull(transform, "transform").build());
        return this;
    }

    /**
     * Applies a raw vanilla transform.
     *
     * @param raw the vanilla transform
     * @return this
     */
    public MirageDisplay transform(AffineTransformation raw) {
        entity.setTransformation(raw);
        return this;
    }

    /**
     * Orients the display to face {@code target}.
     *
     * @param target the target position
     * @return this
     */
    public MirageDisplay lookAt(Vec3d target) {
        Vec3d pos = getPos();
        MirageTransform t = MirageTransform.identity()
                .leftRotation(MirageMath.lookRotation(pos, target));
        return transform(t);
    }

    /** @see #lookAt(Vec3d) */
    public MirageDisplay faceTowards(Vec3d target) {
        return lookAt(target);
    }

    /**
     * Replaces rotation with an absolute X-axis rotation.
     *
     * @param degrees angle in degrees
     * @return this
     */
    public MirageDisplay rotationX(float degrees) {
        return transform(MirageTransform.identity().rotationX(degrees));
    }

    /**
     * Replaces rotation with an absolute Y-axis rotation.
     *
     * @param degrees angle in degrees
     * @return this
     */
    public MirageDisplay rotationY(float degrees) {
        return transform(MirageTransform.identity().rotationY(degrees));
    }

    /**
     * Replaces rotation with an absolute Z-axis rotation.
     *
     * @param degrees angle in degrees
     * @return this
     */
    public MirageDisplay rotationZ(float degrees) {
        return transform(MirageTransform.identity().rotationZ(degrees));
    }

    /**
     * Sets uniform scale.
     *
     * @param uniform the scale factor
     * @return this
     */
    public MirageDisplay scale(float uniform) {
        return transform(MirageTransform.identity().scale(uniform));
    }

    /**
     * Sets non-uniform scale.
     *
     * @param x scale on X
     * @param y scale on Y
     * @param z scale on Z
     * @return this
     */
    public MirageDisplay scale(float x, float y, float z) {
        return transform(MirageTransform.identity().scale(x, y, z));
    }

    /* ---------- Interpolation ---------- */

    /**
     * Sets interpolation duration.
     *
     * @param durationTicks duration in ticks
     * @return this
     */
    public MirageDisplay interpolation(int durationTicks) {
        entity.setInterpolationDuration(Math.max(0, durationTicks));
        return this;
    }

    /**
     * Sets interpolation start delay.
     *
     * @param delayTicks delay in ticks
     * @return this
     */
    public MirageDisplay startInterpolation(int delayTicks) {
        entity.setStartInterpolation(delayTicks);
        return this;
    }

    /**
     * Sets interpolation with zero delay.
     *
     * @param durationTicks duration in ticks
     * @return this
     */
    public MirageDisplay interpolateNow(int durationTicks) {
        entity.setInterpolationDuration(Math.max(0, durationTicks));
        entity.setStartInterpolation(0);
        return this;
    }

    /* ---------- Visual ---------- */

    /**
     * Sets view range multiplier.
     *
     * @param range the multiplier
     * @return this
     */
    public MirageDisplay viewRange(float range) {
        entity.setViewRange(range);
        return this;
    }

    /**
     * Sets shadow parameters.
     *
     * @param radius   shadow radius
     * @param strength shadow strength
     * @return this
     */
    public MirageDisplay shadow(float radius, float strength) {
        entity.setShadowRadius(radius);
        entity.setShadowStrength(strength);
        return this;
    }

    /** Disables shadow. */
    public MirageDisplay noShadow() {
        return shadow(0f, 0f);
    }

    /**
     * Sets billboard mode.
     *
     * @param mode the mode
     * @return this
     */
    public MirageDisplay billboard(DisplayEntity.BillboardMode mode) {
        entity.setBillboardMode(mode);
        return this;
    }

    /**
     * Sets brightness override.
     *
     * @param block block light, 0–15
     * @param sky   sky light, 0–15
     * @return this
     * @throws IllegalArgumentException if out of range
     */
    public MirageDisplay brightness(int block, int sky) {
        MirageValidation.requireRange(block, 0, 15, "block light");
        MirageValidation.requireRange(sky, 0, 15, "sky light");
        entity.setBrightness(new Brightness(block, sky));
        return this;
    }

    /** Sets full brightness (15/15). */
    public MirageDisplay fullBright() {
        return brightness(15, 15);
    }

    /**
     * Sets glow outline color.
     *
     * @param argb ARGB, or -1 to disable
     * @return this
     */
    public MirageDisplay glowColor(int argb) {
        entity.setGlowColorOverride(argb);
        return this;
    }

    /**
     * Toggles vanilla glowing.
     *
     * @param enabled true to enable
     * @return this
     */
    public MirageDisplay glow(boolean enabled) {
        entity.setGlowColorOverride(enabled ? 0xFFFFFFFF : -1);
        return this;
    }

    /* ---------- Position ---------- */

    /**
     * Teleports to coordinates.
     *
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return this
     */
    public MirageDisplay moveTo(double x, double y, double z) {
        entity.setPosition(x, y, z);
        return this;
    }

    /**
     * Teleports to a position.
     *
     * @param pos the position
     * @return this
     */
    public MirageDisplay moveTo(Vec3d pos) {
        entity.setPosition(pos.x, pos.y, pos.z);
        return this;
    }

    /**
     * Teleports with interpolation.
     *
     * @param pos           the position
     * @param durationTicks interpolation duration
     * @return this
     */
    public MirageDisplay moveTo(Vec3d pos, int durationTicks) {
        interpolateNow(durationTicks);
        entity.setPosition(pos.x, pos.y, pos.z);
        return this;
    }

    /**
     * Adds an offset to the current position.
     *
     * @param x delta X
     * @param y delta Y
     * @param z delta Z
     * @return this
     */
    public MirageDisplay offset(double x, double y, double z) {
        Vec3d pos = getPos();
        return moveTo(pos.x + x, pos.y + y, pos.z + z);
    }

    /** Hides the display. */
    public MirageDisplay hide() {
        return viewRange(0f);
    }

    /** Shows the display. */
    public MirageDisplay show() {
        return viewRange(1.0f);
    }

    /** @return the current position */
    public Vec3d getPos() {
        return entity.getPos();
    }

    /* ---------- Lifecycle ---------- */

    /** Spawns the entity into the world. */
    public MirageDisplay spawn() {
        world.spawnEntity(entity);
        return this;
    }

    /** Removes the entity from the world. */
    public void remove() {
        entity.discard();
    }

    /** @return true if the entity is alive */
    public boolean isAlive() {
        return entity.isAlive();
    }

    /* ---------- Accessors ---------- */

    /** @return the backing entity */
    public DisplayEntity getEntity() {
        return entity;
    }

    /** @return the world */
    public ServerWorld getWorld() {
        return world;
    }

    /** @return the UUID */
    public UUID getUuid() {
        return entity.getUuid();
    }

    /** @return the network entity ID */
    public int getEntityId() {
        return entity.getId();
    }

    /* ---------- User data ---------- */

    /**
     * Stores arbitrary data.
     *
     * @param key   the key
     * @param value the value
     * @return this
     */
    public MirageDisplay data(String key, Object value) {
        userData.put(key, value);
        return this;
    }

    /**
     * Retrieves stored data.
     *
     * @param key the key
     * @return the value, or null
     */
    public Object data(String key) {
        return userData.get(key);
    }

    /**
     * Removes stored data.
     *
     * @param key the key
     * @return this
     */
    public MirageDisplay removeData(String key) {
        userData.remove(key);
        return this;
    }

    /* ---------- Clone ---------- */

    /**
     * Copies visual settings from another display.
     * Position and UUID are not copied.
     *
     * @param source the source display
     * @return this
     */
    public MirageDisplay cloneSettingsFrom(MirageDisplay source) {
        MirageValidation.requireNonNull(source, "source");
        DisplayEntity e = source.getEntity();
        this.transform(e.getTransformation(e.getDataTracker()));
        this.interpolation(e.getInterpolationDuration());
        this.viewRange(e.getViewRange());
        this.shadow(e.getShadowRadius(), e.getShadowStrength());
        this.billboard(e.getBillboardMode());
        int packed = e.getBrightness();
        if (packed != -1) {
            this.brightness(Brightness.unpack(packed).block(), Brightness.unpack(packed).sky());
        }
        this.glowColor(e.getGlowColorOverride());
        return this;
    }
}