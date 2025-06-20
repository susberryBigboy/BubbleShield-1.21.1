package com.papack.bubbleshield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
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
    private static final float SHIELD_RADIUS = 4.0f;
    private static final int SHIELD_DURATION_TICKS = 200;
    private static final int DEPLOY_ANIMATION_TICKS = 20;

    private final Set<UUID> reflectedProjectiles = new HashSet<>();


    @Nullable
    private UUID ownerUuid;

    private int age = 0;

    public BubbleShieldEntity(World world, double x, double y, double z) {
        super(BUBBLE_SHIELD, world);
        this.setPosition(x, y, z);
    }

    public BubbleShieldEntity(EntityType<BubbleShieldEntity> bubbleShieldEntityEntityType, World world) {
        super(bubbleShieldEntityEntityType, world);
        this.noClip = true;
    }


    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // DataTrackerは現時点では使っていませんが、将来的な拡張のために必要です
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (age >= SHIELD_DURATION_TICKS) {
            discard();
            return;
        }

        float currentRadius = SHIELD_RADIUS * getAnimatedScale(0.0f);
        updateBoundingBox(currentRadius);

        if (!getWorld().isClient) {
            Vec3d center = this.getPos();

            for (Entity e : getWorld().getOtherEntities(this, getBoundingBox().expand(0.5))) {
                Vec3d toEntity = e.getPos().subtract(center);
                double distance = toEntity.length();

                if (distance <= (double) SHIELD_RADIUS) {
                    // --- 投射物のオーナーチェックを共通化 ---
                    if (e instanceof ProjectileEntity projectile) {
                        if (projectile.getOwner() instanceof LivingEntity projectileOwner) {
                            // 投射物のオーナーがPlayerで、かつシールドのオーナーと同じであれば、通過させる
                            if (projectileOwner instanceof PlayerEntity ownerPlayer && isOwner(ownerPlayer.getUuid())) {
                                continue; // この投射物はシールドを通り抜ける
                            }
                        }
                        // ここに来るのは、オーナー以外の投射物、またはオーナー不明の投射物
                        // そのため、以下のswitch文で反射・落下などの処理を行う
                    }

                    // --- エンティティのタイプごとの個別処理 ---
                    switch (e) {
                        case ArrowEntity arrow -> {
                            // ここに来るのはオーナー以外の弓矢
                            handleProjectileReaction(arrow, center, toEntity);
                            continue;
                        }

                        case TridentEntity trident -> {
                            // ここに来るのはオーナー以外のトライデント
                            handleProjectileReaction(trident, center, toEntity);
                            continue;
                        }

                        case WindChargeEntity windCharge -> {
                            // WindChargeは通常オーナー概念が薄いので、ここで直接爆発させる
                            e.getWorld().createExplosion(null, e.getX(), e.getY(), e.getZ(),
                                    1.0f, World.ExplosionSourceType.MOB);
                            windCharge.discard();
                            continue;
                        }
                        case FireballEntity fireball -> {
                            // FireballもgetOwner()を持たない場合があるので、ここで直接爆発させる
                            e.getWorld().createExplosion(null, e.getX(), e.getY(), e.getZ(),
                                    1.0f, World.ExplosionSourceType.MOB);
                            fireball.discard();
                            continue;
                        }

                        // LivingEntity（Mobやプレイヤー）のノックバック処理
                        case LivingEntity livingEntity -> {
                            // PlayerEntityであれば、オーナー/許可設定に基づいてノックバックを判断
                            if (livingEntity instanceof PlayerEntity player) {
                                // オーナーであるか、他のプレイヤーを許可する設定であれば通過
                                if (isOwner(player.getUuid()) || allowOtherPlayersInside()) {
                                    continue; // 通過させる
                                }
                            }
                            // それ以外のLivingEntity（Mob）またはノックバックが必要なプレイヤーはここでノックバック
                            knockBackEntity(e, toEntity);
                        }
                        default -> {
                            // その他のエンティティ（アイテムエンティティなど）に対するデフォルト処理
                            // 必要に応じてここで処理を追加
                        }
                    }

                    if (age % 100 == 0) {
                        reflectedProjectiles.clear(); // 定期的に整理
                    }
                }
            }
        }
    }

    public float getAnimatedScale(float tickDelta) {
        return Math.min((age + tickDelta) / (float) DEPLOY_ANIMATION_TICKS, 1.0f);
    }

    private void updateBoundingBox(float currentRadius) {
        setBoundingBox(new Box(
                getX() - currentRadius, getY() - currentRadius, getZ() - currentRadius,
                getX() + currentRadius, getY() + currentRadius, getZ() + currentRadius
        ));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("OwnerUuid")) {
            this.ownerUuid = nbt.getUuid("OwnerUuid");
        }
        this.age = nbt.getInt("Age");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null) {
            nbt.putUuid("OwnerUuid", this.ownerUuid);
        }
        nbt.putInt("Age", this.age);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false; // Invulnerable
    }

    private void knockBackEntity(Entity e, Vec3d toEntity) {
        Vec3d knock = toEntity.normalize().multiply(0.5);
        e.addVelocity(knock.x, 0.1, knock.z);
        e.velocityModified = true;
    }

    private boolean allowOtherPlayersInside() {
        return false; // コンフィグなどに応じて切り替え可
    }

    public void setOwner(@Nullable UUID uuid) {
        this.ownerUuid = uuid;
    }

    @Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    private boolean isOwner(UUID uuid) {
        return this.ownerUuid != null && this.ownerUuid.equals(uuid);
    }

    // --- 新しく追加するヘルパーメソッド ---
    private void handleProjectileReaction(ProjectileEntity projectile, Vec3d center, Vec3d toEntity) {
        if (!reflectedProjectiles.contains(projectile.getUuid())) {
            // シールド表面の衝突点を近似計算
            Vec3d collisionPoint = center.add(toEntity.normalize().multiply(SHIELD_RADIUS));

            // 投射物を停止させ、落下させる
            projectile.setVelocity(0, -0.1, 0);
            if (projectile instanceof ArrowEntity arrow) {
                arrow.setNoClip(false);
                arrow.setCritical(false); // クリティカルもArrowEntityにのみ適用
            } else if (projectile instanceof TridentEntity trident) {
                trident.setNoClip(false);
            }
            projectile.velocityModified = true;
            projectile.setPosition(collisionPoint.x, collisionPoint.y, collisionPoint.z);

            // 音を鳴らす
            getWorld().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                    SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
            reflectedProjectiles.add(projectile.getUuid());
        }
    }
}

