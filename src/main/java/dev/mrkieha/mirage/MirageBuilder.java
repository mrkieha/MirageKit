package dev.mrkieha.mirage;

import dev.mrkieha.mirage.util.MirageTransform;
import dev.mrkieha.mirage.util.MirageValidation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;

/**
 * Fluent factory for {@link MirageDisplay}.
 *
 * @since 1.0
 */
public class MirageBuilder {

    private final ServerWorld world;

    private BlockState blockState = Blocks.STONE.getDefaultState();
    private ItemStack itemStack   = ItemStack.EMPTY;
    private Text displayText      = null;
    private EntityType<? extends DisplayEntity> entityType = EntityType.BLOCK_DISPLAY;

    private Vec3d position                    = Vec3d.ZERO;
    private MirageTransform transform         = MirageTransform.identity();
    private int interpolationDuration         = 0;
    private float viewRange                   = 1.0f;
    private float shadowRadius                = 0.0f;
    private float shadowStrength              = 1.0f;
    private DisplayEntity.BillboardMode billboard = DisplayEntity.BillboardMode.FIXED;
    private int glowColor                     = -1;
    private int brightnessBlock               = -1;
    private int brightnessSky                 = -1;

    private MirageBuilder(ServerWorld world) {
        this.world = MirageValidation.requireNonNull(world, "world");
    }

    /**
     * Creates a builder for the given world.
     *
     * @param world the world
     * @return a new builder
     */
    public static MirageBuilder in(ServerWorld world) {
        return new MirageBuilder(world);
    }

    /* ---------- Content ---------- */

    /**
     * Sets block content.
     *
     * @param state the block state
     * @return this
     */
    public MirageBuilder block(BlockState state) {
        this.blockState = state;
        this.entityType = EntityType.BLOCK_DISPLAY;
        return this;
    }

    /**
     * Sets item content.
     *
     * @param stack the item stack
     * @return this
     */
    public MirageBuilder item(ItemStack stack) {
        this.itemStack = stack;
        this.entityType = EntityType.ITEM_DISPLAY;
        return this;
    }

    /**
     * Sets text content.
     *
     * @param text the text
     * @return this
     */
    public MirageBuilder text(Text text) {
        this.displayText = text;
        this.entityType = EntityType.TEXT_DISPLAY;
        return this;
    }

    /* ---------- Position ---------- */

    /**
     * Sets position from coordinates.
     *
     * @param x world X
     * @param y world Y
     * @param z world Z
     * @return this
     */
    public MirageBuilder at(double x, double y, double z) {
        this.position = new Vec3d(x, y, z);
        return this;
    }

    /**
     * Sets position from a vector.
     *
     * @param pos the position
     * @return this
     */
    public MirageBuilder at(Vec3d pos) {
        this.position = pos;
        return this;
    }

    /* ---------- Transform ---------- */

    /**
     * Sets the initial transform.
     *
     * @param transform the transform
     * @return this
     */
    public MirageBuilder transform(MirageTransform transform) {
        this.transform = MirageValidation.requireNonNull(transform, "transform");
        return this;
    }

    /**
     * Sets a raw vanilla transform.
     *
     * @param raw the vanilla transform
     * @return this
     */
    public MirageBuilder rawTransform(AffineTransformation raw) {
        this.transform = MirageTransform.fromVanilla(raw);
        return this;
    }

    /**
     * Sets uniform scale.
     *
     * @param uniform the scale factor
     * @return this
     */
    public MirageBuilder scale(float uniform) {
        this.transform.scale(uniform);
        return this;
    }

    /**
     * Sets non-uniform scale.
     *
     * @param x scale on X
     * @param y scale on Y
     * @param z scale on Z
     * @return this
     */
    public MirageBuilder scale(float x, float y, float z) {
        this.transform.scale(x, y, z);
        return this;
    }

    /**
     * Accumulates rotation on all axes.
     *
     * @param x degrees on X
     * @param y degrees on Y
     * @param z degrees on Z
     * @return this
     */
    public MirageBuilder rotate(float x, float y, float z) {
        this.transform.rotate(x, y, z);
        return this;
    }

    /**
     * Accumulates Y-axis rotation.
     *
     * @param degrees angle in degrees
     * @return this
     */
    public MirageBuilder rotateY(float degrees) {
        this.transform.rotateY(degrees);
        return this;
    }

    /* ---------- Interpolation ---------- */

    /**
     * Sets interpolation duration.
     *
     * @param ticks duration in ticks
     * @return this
     */
    public MirageBuilder interpolation(int ticks) {
        this.interpolationDuration = ticks;
        return this;
    }

    /* ---------- Visual ---------- */

    /**
     * Sets view range multiplier.
     *
     * @param range the multiplier
     * @return this
     */
    public MirageBuilder viewRange(float range) {
        this.viewRange = range;
        return this;
    }

    /**
     * Sets shadow parameters.
     *
     * @param radius   shadow radius
     * @param strength shadow strength
     * @return this
     */
    public MirageBuilder shadow(float radius, float strength) {
        this.shadowRadius   = radius;
        this.shadowStrength = strength;
        return this;
    }

    /** Disables shadow. */
    public MirageBuilder noShadow() {
        return shadow(0f, 0f);
    }

    /**
     * Sets billboard mode.
     *
     * @param mode the mode
     * @return this
     */
    public MirageBuilder billboard(DisplayEntity.BillboardMode mode) {
        this.billboard = mode;
        return this;
    }

    /**
     * Sets glow outline color.
     *
     * @param argb ARGB, or -1 to disable
     * @return this
     */
    public MirageBuilder glowColor(int argb) {
        this.glowColor = argb;
        return this;
    }

    /**
     * Toggles vanilla glowing.
     *
     * @param enabled true to enable
     * @return this
     */
    public MirageBuilder glow(boolean enabled) {
        this.glowColor = enabled ? 0xFFFFFFFF : -1;
        return this;
    }

    /**
     * Sets brightness override.
     *
     * @param block block light, 0–15
     * @param sky   sky light, 0–15
     * @return this
     */
    public MirageBuilder brightness(int block, int sky) {
        this.brightnessBlock = block;
        this.brightnessSky   = sky;
        return this;
    }

    /** Sets full brightness (15/15). */
    public MirageBuilder fullBright() {
        return brightness(15, 15);
    }

    /* ---------- Clone ---------- */

    /**
     * Copies settings from an existing display.
     * Position and UUID are not copied.
     *
     * @param source the source display
     * @return this
     */
    public MirageBuilder cloneFrom(MirageDisplay source) {
        MirageValidation.requireNonNull(source, "source");
        DisplayEntity e = source.getEntity();
        this.interpolationDuration = e.getInterpolationDuration();
        this.viewRange = e.getViewRange();
        this.shadowRadius = e.getShadowRadius();
        this.shadowStrength = e.getShadowStrength();
        this.billboard = e.getBillboardMode();
        int packed = e.getBrightness();
        if (packed != -1) {
            this.brightness(Brightness.unpack(packed).block(), Brightness.unpack(packed).sky());
        }
        this.glowColor = e.getGlowColorOverride();
        this.transform = MirageTransform.fromVanilla(e.getTransformation(e.getDataTracker()));

        if (e instanceof DisplayEntity.BlockDisplayEntity) {
            this.block(((DisplayEntity.BlockDisplayEntity) e).getBlockState());
        } else if (e instanceof DisplayEntity.ItemDisplayEntity) {
            this.item(((DisplayEntity.ItemDisplayEntity) e).getItemStack());
        } else if (e instanceof DisplayEntity.TextDisplayEntity) {
            this.text(((DisplayEntity.TextDisplayEntity) e).getText());
        }
        return this;
    }

    /* ---------- Build ---------- */

    /**
     * Builds the display without spawning.
     *
     * @return the configured display
     */
    public MirageDisplay build() {
        DisplayEntity entity;
        if (entityType == EntityType.ITEM_DISPLAY) {
            entity = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
            ((DisplayEntity.ItemDisplayEntity) entity).setItemStack(itemStack);
        } else if (entityType == EntityType.TEXT_DISPLAY) {
            entity = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
            if (displayText != null) {
                ((DisplayEntity.TextDisplayEntity) entity).setText(displayText);
            }
        } else {
            entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            ((DisplayEntity.BlockDisplayEntity) entity).setBlockState(blockState);
        }

        entity.setPosition(position.x, position.y, position.z);
        entity.setTransformation(transform.build());
        entity.setInterpolationDuration(interpolationDuration);
        entity.setViewRange(viewRange);
        entity.setShadowRadius(shadowRadius);
        entity.setShadowStrength(shadowStrength);
        entity.setBillboardMode(billboard);

        if (glowColor != -1) {
            entity.setGlowColorOverride(glowColor);
        }

        if (brightnessBlock >= 0 && brightnessSky >= 0) {
            entity.setBrightness(new Brightness(brightnessBlock, brightnessSky));
        }

        return new MirageDisplay(entity, world);
    }

    /**
     * Builds and spawns the display.
     *
     * @return the spawned display
     */
    public MirageDisplay buildAndSpawn() {
        MirageDisplay display = build();
        display.spawn();
        return display;
    }
}