package com.papack.bubbleshield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.papack.bubbleshield.ModEntities.BUBBLE_SHIELD;

public class BubbleShieldEntity extends Entity {
    private static final float SHIELD_RADIUS = 5.0f;
    private static final int SHIELD_DURATION_TICKS = 200;
    private static final int DEPLOY_ANIMATION_TICKS = 20;
    private static final float RETRACT_ANIMATION_MULTIPLIER = 0.5f;
    private static final float RETRACT_ANIMATION_TICKS = DEPLOY_ANIMATION_TICKS * RETRACT_ANIMATION_MULTIPLIER;

    private BubbleShieldType type = BubbleShieldType.BASE;
    private static final TrackedData<String> TYPE = DataTracker.registerData(BubbleShieldEntity.class, TrackedDataHandlerRegistry.STRING);

    private boolean allowOthers = false;
    private boolean hasTeleportedOwner = false;

    private final Set<UUID> reflectedProjectiles = new HashSet<>();

    @Nullable
    private UUID ownerUuid;

    private int age = 0;
    private boolean spawnSoundPlayed = false;
    private boolean retracting = false;
    private int retractAge = 0;

    public BubbleShieldEntity(World world, double x, double y, double z) {
        super(BUBBLE_SHIELD, world);
        this.setPosition(x, y, z);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public BubbleShieldEntity(EntityType<BubbleShieldEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
        System.out.println("[HEAL] " + type);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TYPE, BubbleShieldType.BASE.name());
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isClient && !spawnSoundPlayed && age == 0) {
            getWorld().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);
            spawnSoundPlayed = true;
        }

        if (!retracting) {
            age++;
            if (age >= SHIELD_DURATION_TICKS) {
                retracting = true;
                retractAge = 0;
            }
        } else {
            retractAge++;
            if (retractAge >= RETRACT_ANIMATION_TICKS) {
                if (!getWorld().isClient()) {
                    getWorld().playSound(null, getX(), getY(), getZ(),
                            SoundEvents.BLOCK_BEACON_DEACTIVATE,
                            SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                discard();
                return;
            }
        }

        float currentScale = getAnimatedScale(0.0f);
        float currentRadius = SHIELD_RADIUS * currentScale;
        updateBoundingBox(currentRadius);

        if (!getWorld().isClient) {
            Vec3d center = this.getPos();

            for (Entity e : getWorld().getOtherEntities(this, getBoundingBox())) {
                Vec3d toEntity = e.getPos().subtract(center);
                double distance = toEntity.length();

                if (retracting && (e instanceof ProjectileEntity || e instanceof LivingEntity)) {
                    continue;
                }

                // In Shield
                if (distance <= SHIELD_RADIUS) {
                    // Healing
                    if (type == BubbleShieldType.HEALING && age % 20 == 0 && e instanceof PlayerEntity player) {
                        if (isOwner(player.getUuid()) || allowOthers) {
                            if (player.getHealth() < player.getMaxHealth()) {
                                player.heal(2.0F);
                            }
                        }
                    }

                    // Teleport
                    if (this.type == BubbleShieldType.TELEPORT && getAnimatedScale(0) >= 1.0f) {
                        if (!hasTeleportedOwner) {
                            TeleportHandler.tryTeleportOwnerOnly(this);
                            hasTeleportedOwner = true;
                        }
                        TeleportHandler.tryTeleportOthers(this);
                    }


                    if (e instanceof ProjectileEntity projectile) {
                        if (projectile.getOwner() instanceof LivingEntity owner) {
                            if (owner instanceof PlayerEntity player && isOwner(player.getUuid())) {
                                continue;
                            }
                        }
                    }

                    switch (e) {
                        case ArrowEntity arrow -> handleProjectileReaction(arrow, center, toEntity);
                        case TridentEntity trident -> handleProjectileReaction(trident, center, toEntity);
                        case WindChargeEntity windCharge -> {
                            e.getWorld().createExplosion(null, e.getX(), e.getY(), e.getZ(), 1.0f, World.ExplosionSourceType.MOB);
                            windCharge.discard();
                        }
                        case FireballEntity fireball -> {
                            e.getWorld().createExplosion(null, e.getX(), e.getY(), e.getZ(), 1.0f, World.ExplosionSourceType.MOB);
                            fireball.discard();
                        }
                        case LivingEntity living -> {
                            if (living instanceof PlayerEntity player) {
                                if (isOwner(player.getUuid()) || allowOthers) continue;
                            }
                            knockBackEntity(e, toEntity);
                        }
                        default -> {
                        }
                    }

                    if (age % 100 == 0) {
                        reflectedProjectiles.clear();
                    }
                }
            }
        }
    }

    public void setType(BubbleShieldType type) {
        this.type = type;
        if (!this.getWorld().isClient) {
            this.dataTracker.set(TYPE, type.name());
        }
    }

    public BubbleShieldType getTypeEnum() {
        if (this.getWorld().isClient) {
            return BubbleShieldType.valueOf(this.dataTracker.get(TYPE));
        }
        return this.type;
    }

    public float getAnimatedScale(float tickDelta) {
        if (retracting) {
            float progress = (retractAge + tickDelta) / RETRACT_ANIMATION_TICKS;
            return 1.0f - Math.min(progress, 1.0f);
        } else {
            return Math.min((age + tickDelta) / (float) DEPLOY_ANIMATION_TICKS, 1.0f);
        }
    }

    private void updateBoundingBox(float radius) {
        double margin = 0.5;
        setBoundingBox(new Box(
                getX() - radius - margin, getY() - radius - margin, getZ() - radius - margin,
                getX() + radius + margin, getY() + radius + margin, getZ() + radius + margin
        ));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("OwnerUuid")) this.ownerUuid = nbt.getUuid("OwnerUuid");
        this.age = nbt.getInt("Age");
        this.spawnSoundPlayed = nbt.getBoolean("SpawnSoundPlayed");
        this.retracting = nbt.getBoolean("Retracting");
        this.retractAge = nbt.getInt("RetractAge");
        this.allowOthers = nbt.getBoolean("AllowOthers");
        this.hasTeleportedOwner = nbt.getBoolean("HasTeleportedOwner");
        if (nbt.contains("ShieldType")) this.type = BubbleShieldType.valueOf(nbt.getString("ShieldType"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (ownerUuid != null) nbt.putUuid("OwnerUuid", ownerUuid);
        nbt.putInt("Age", age);
        nbt.putBoolean("SpawnSoundPlayed", spawnSoundPlayed);
        nbt.putBoolean("Retracting", retracting);
        nbt.putInt("RetractAge", retractAge);
        nbt.putBoolean("AllowOthers", allowOthers);
        nbt.putString("ShieldType", type.name());
        nbt.putBoolean("HasTeleportedOwner", hasTeleportedOwner);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    private void knockBackEntity(Entity e, Vec3d toEntity) {
        Vec3d knockDir = toEntity.normalize();
        double pushDistance = SHIELD_RADIUS - toEntity.length();
        if (pushDistance > 0) {
            Vec3d pushPos = e.getPos().add(knockDir.multiply(pushDistance + 0.05));
            e.setPosition(pushPos.x, pushPos.y, pushPos.z);
        }
        Vec3d knockVelocity = knockDir.multiply(0.5);
        e.addVelocity(knockVelocity.x, 0.1, knockVelocity.z);
        e.velocityModified = true;
    }

    public void setAllowOthers(boolean allow) {
        this.allowOthers = allow;
    }

    public void setOwner(@Nullable UUID uuid) {
        this.ownerUuid = uuid;
    }

    public @Nullable UUID getOwnerUuid() {
        return ownerUuid;
    }

    public boolean isOwner(UUID uuid) {
        return ownerUuid != null && ownerUuid.equals(uuid);
    }

    private void handleProjectileReaction(ProjectileEntity projectile, Vec3d center, Vec3d toEntity) {
        if (!reflectedProjectiles.contains(projectile.getUuid())) {
            Vec3d collisionPoint = center.add(toEntity.normalize().multiply(SHIELD_RADIUS));
            projectile.setVelocity(0, -0.1, 0);
            if (projectile instanceof ArrowEntity arrow) {
                arrow.setNoClip(false);
                arrow.setCritical(false);
            } else if (projectile instanceof TridentEntity trident) {
                trident.setNoClip(false);
            }
            projectile.velocityModified = true;
            projectile.setPosition(collisionPoint.x, collisionPoint.y, collisionPoint.z);
            getWorld().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                    SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
            reflectedProjectiles.add(projectile.getUuid());
        }
    }

    public boolean allowsOthers() {
        return allowOthers;
    }
}
