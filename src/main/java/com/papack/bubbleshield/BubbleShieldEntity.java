package com.papack.bubbleshield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
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

    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (age >= SHIELD_DURATION_TICKS) {
            discard();
            return;
        }

        updateBoundingBox(); // AABBを更新

        if (!getWorld().isClient) {
            Vec3d center = this.getPos();

            for (Entity e : getWorld().getOtherEntities(this, getBoundingBox().expand(0.5))) {
                Vec3d toEntity = e.getPos().subtract(center);
                double distance = toEntity.length();

                if (distance <= (double) SHIELD_RADIUS) {
                    if (!getWorld().isClient) {

                        // 弓矢（ArrowEntity）
                        // 弓矢（ArrowEntity）
                        switch (e) {
                            case ArrowEntity arrow -> {
                                // 所有者が放った弓矢かどうかを判定
                                // ArrowEntity には getOwner() メソッドがあるのでそれを利用します
                                if (arrow.getOwner() instanceof net.minecraft.entity.player.PlayerEntity ownerPlayer &&
                                        ownerPlayer.getUuid().equals(this.getOwnerUuid())) {
                                    // シールド所有者の弓矢は衝突判定を行わず、そのまま通過させる
                                    continue; // 次のエンティティのチェックへ
                                }

                                // 所有者以外の弓矢、または所有者不明の弓矢の場合
                                if (!reflectedProjectiles.contains(arrow.getUuid())) {
                                    // 弓矢がシールド表面のどこに当たったかを取得（近似）
                                    // エンティティの中心からシールドの中心へのベクトル (toEntity) を利用
                                    Vec3d collisionPoint = center.add(toEntity.normalize().multiply(SHIELD_RADIUS));

                                    arrow.setVelocity(0, -0.1, 0); // 落下方向へ
                                    arrow.setNoClip(false);       // クリップ無効を解除
                                    arrow.setCritical(false);     // クリティカルを解除
                                    arrow.velocityModified = true; // 速度変更を適用

                                    // 弓矢を当たった場所の少し外側に移動させてから落とす
                                    // これにより、シールドの中心ではなく、当たった場所から落ちるように見えます
                                    arrow.setPosition(collisionPoint.x, collisionPoint.y, collisionPoint.z);

                                    getWorld().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(),
                                            SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                                    reflectedProjectiles.add(arrow.getUuid());
                                }
                                continue;
                            }


                            // トライデント（TridentEntity）
                            case TridentEntity trident -> {
                                if (!reflectedProjectiles.contains(trident.getUuid())) {
                                    trident.setVelocity(0, -0.1, 0); // triden を使う
                                    getWorld().playSound(null, trident.getX(), trident.getY(), trident.getZ(),
                                            SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                                    reflectedProjectiles.add(trident.getUuid());
                                }
                                continue;
                            }


                            // ウインドチャージ・火の玉 → 爆発させる（音なし）
                            // FireballEntity は複数の種類があるので、適切なものを選択するか、共通の親クラスで判定
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


                            // 敵対Mob
                            case net.minecraft.entity.mob.MobEntity mob when mob.getTarget() != null -> {
                                knockBackEntity(e, toEntity);
                                continue;
                            }


                            // 怒ったゴーレム（簡易）
                            // ゴーレムの種類が特定できれば、IronGolemEntity などで判定
                            //case net.minecraft.entity.passive.IronGolemEntity ironGolemEntity -> {
                            case net.minecraft.entity.passive.IronGolemEntity ignored -> {
                                knockBackEntity(e, toEntity);
                                continue;
                            }


                            // プレイヤー
                            case net.minecraft.entity.player.PlayerEntity player -> {
                                if (!player.getUuid().equals(this.getOwnerUuid()) && !allowOtherPlayersInside()) {
                                    knockBackEntity(e, toEntity);
                                }
                            }
                            default -> {
                            }
                        }

                        if (age % 100 == 0) {
                            reflectedProjectiles.clear(); // 定期的に整理
                        }
                    }
                }
            }
        }
    }


    public float getAnimatedScale(float tickDelta) {
        return Math.min((age + tickDelta) / (float) DEPLOY_ANIMATION_TICKS, 1.0f);
    }

    private void updateBoundingBox() {
        double r = SHIELD_RADIUS;
        setBoundingBox(new Box(
                getX() - r, getY() - r, getZ() - r,
                getX() + r, getY() + r, getZ() + r
        ));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
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
}

