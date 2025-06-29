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
    private static final int DEPLOY_ANIMATION_TICKS = 20; // 展開アニメーション時間
    private static final float RETRACT_ANIMATION_MULTIPLIER = 0.5f; // 縮小アニメーション時間の倍率
    private static final float RETRACT_ANIMATION_TICKS = DEPLOY_ANIMATION_TICKS * RETRACT_ANIMATION_MULTIPLIER; // 縮小アニメーション時間

    private final Set<UUID> reflectedProjectiles = new HashSet<>();

    @Nullable
    private UUID ownerUuid;

    private int age = 0;
    private boolean spawnSoundPlayed = false;
    private boolean retracting = false; // ★追加: 縮小中かどうかを示すフラグ
    private int retractAge = 0; // ★追加: 縮小アニメーションの進行度

    public BubbleShieldEntity(World world, double x, double y, double z) {
        super(BUBBLE_SHIELD, world);
        this.setPosition(x, y, z);
        this.noClip = true;
        this.setNoGravity(true);
    }

    public BubbleShieldEntity(EntityType<BubbleShieldEntity> bubbleShieldEntityEntityType, World world) {
        super(bubbleShieldEntityEntityType, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // DataTrackerは現時点では使っていませんが、将来的な拡張のために必要です
    }

    @Override
    public void tick() {
        super.tick();

        // 出現音の再生
        if (!getWorld().isClient && !spawnSoundPlayed && age == 0) { // age == 0 で最初のティックに再生
            getWorld().playSound(null, getX(), getY(), getZ(),
                    SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);
            spawnSoundPlayed = true;
        }

        // ★修正: シールドの寿命が来たときの処理
        if (!retracting) { // まだ縮小フェーズに入っていない場合
            age++; // 通常の経過時間を進める
            if (age >= SHIELD_DURATION_TICKS) {
                retracting = true; // 縮小フェーズを開始
                retractAge = 0; // 縮小アニメーションのカウンターをリセット
            }
        } else { // 縮小フェーズ中の場合
            retractAge++; // 縮小アニメーションの時間を進める
            if (retractAge >= RETRACT_ANIMATION_TICKS) {
                // 消滅音の再生 (サーバー側でのみ)
                if (!getWorld().isClient()) {
                    getWorld().playSound(null, getX(), getY(), getZ(),
                            SoundEvents.BLOCK_BEACON_DEACTIVATE,
                            SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                discard(); // エンティティを消去
                return; // ここで処理を終了
            }
        }

        // ★修正: アニメーションスケールの計算にretracting状態を考慮
        float currentScale = getAnimatedScale(0.0f);
        float currentRadius = SHIELD_RADIUS * currentScale;
        updateBoundingBox(currentRadius);

        if (!getWorld().isClient) {
            Vec3d center = this.getPos();

            for (Entity e : getWorld().getOtherEntities(this, getBoundingBox().expand(0.5))) {
                Vec3d toEntity = e.getPos().subtract(center);
                double distance = toEntity.length();

                // 縮小中のシールドからはエンティティを弾かないようにする（必要であれば調整）
                if (retracting) {
                    // 縮小中は物理的な影響を弱めるか、完全に無効にする
                    // 例: 縮小中は投射物やLivingEntityに対する処理をスキップ
                    if (e instanceof ProjectileEntity || e instanceof LivingEntity) {
                        continue;
                    }
                }

                if (distance <= (double) SHIELD_RADIUS) { // シールド範囲内かチェック (SHIELD_RADIUSは最大半径)
                    // --- 投射物のオーナーチェックを共通化 ---
                    if (e instanceof ProjectileEntity projectile) {
                        if (projectile.getOwner() instanceof LivingEntity projectileOwner) {
                            if (projectileOwner instanceof PlayerEntity ownerPlayer && isOwner(ownerPlayer.getUuid())) {
                                continue;
                            }
                        }
                    }

                    // --- エンティティのタイプごとの個別処理 ---
                    switch (e) {
                        case ArrowEntity arrow -> {
                            handleProjectileReaction(arrow, center, toEntity);
                            continue;
                        }
                        case TridentEntity trident -> {
                            handleProjectileReaction(trident, center, toEntity);
                            continue;
                        }
                        case WindChargeEntity windCharge -> {
                            e.getWorld().createExplosion(null, e.getX(), e.getY(), e.getZ(),
                                    1.0f, World.ExplosionSourceType.MOB);
                            windCharge.discard();
                            continue;
                        }
                        case FireballEntity fireball -> {
                            e.getWorld().createExplosion(null, e.getX(), e.getY(), e.getZ(),
                                    1.0f, World.ExplosionSourceType.MOB);
                            fireball.discard();
                            continue;
                        }
                        case LivingEntity livingEntity -> {
                            if (livingEntity instanceof PlayerEntity player) {
                                if (isOwner(player.getUuid()) || allowOtherPlayersInside()) {
                                    continue;
                                }
                            }
                            knockBackEntity(e, toEntity);
                        }
                        default -> {
                            // その他のエンティティ
                        }
                    }

                    if (age % 100 == 0) {
                        reflectedProjectiles.clear();
                    }
                }
            }
        }
    }

    public float getAnimatedScale(float tickDelta) {
        if (retracting) {
            // 縮小アニメーションのスケールを計算
            // retractAge / RETRACT_ANIMATION_TICKS で 1.0 から 0.0 へ線形に減少
            float progress = (retractAge + tickDelta) / RETRACT_ANIMATION_TICKS;
            return 1.0f - Math.min(progress, 1.0f); // 1.0から0.0に縮小
        } else {
            // 展開アニメーションのスケールを計算
            return Math.min((age + tickDelta) / (float) DEPLOY_ANIMATION_TICKS, 1.0f);
        }
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
        this.spawnSoundPlayed = nbt.getBoolean("SpawnSoundPlayed");
        // ★追加: retracting と retractAge もNBTから読み込む
        this.retracting = nbt.getBoolean("Retracting");
        this.retractAge = nbt.getInt("RetractAge");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.ownerUuid != null) {
            nbt.putUuid("OwnerUuid", this.ownerUuid);
        }
        nbt.putInt("Age", this.age);
        nbt.putBoolean("SpawnSoundPlayed", this.spawnSoundPlayed);
        // ★追加: retracting と retractAge もNBTに書き込む
        nbt.putBoolean("Retracting", this.retracting);
        nbt.putInt("RetractAge", this.retractAge);
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

    /*@Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }*/

    private boolean isOwner(UUID uuid) {
        return this.ownerUuid != null && this.ownerUuid.equals(uuid);
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
}