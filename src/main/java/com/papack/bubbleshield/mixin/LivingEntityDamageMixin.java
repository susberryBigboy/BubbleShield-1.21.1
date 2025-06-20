package com.papack.bubbleshield.mixin;

import com.papack.bubbleshield.BubbleShieldEntity;
import com.papack.bubbleshield.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void bubbleShield_preventExplosionDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        World world = self.getWorld();
        if (world.isClient) {
            return;
        }

        // --- ダメージが爆発によるものか、または発生源がクリーパーかを確認 ---
        boolean isExplosionDamage = (source.isOf(DamageTypes.EXPLOSION)
                || source.isOf(DamageTypes.PLAYER_EXPLOSION)
                || source.isOf(DamageTypes.SONIC_BOOM)
                || source.isOf(DamageTypes.MOB_ATTACK));

        // source.getSource() がエンティティを返し、それがクリーパーであるかを確認
        // これにより、DamageTypes.EXPLOSIONでカバーしきれないクリーパー爆発も捕捉できる
        boolean isCreeperExplosionSource = false;
        Entity damageSourceEntity = source.getSource();
        if (damageSourceEntity != null) {
            // クリーパーのEntityTypeは net.minecraft.entity.EntityType.CREEPER
            if (damageSourceEntity.getType() == EntityType.CREEPER) {
                isCreeperExplosionSource = true;
            }
        }

        if (isExplosionDamage || isCreeperExplosionSource || source.isDirect()) { // どちらかの条件を満たせば処理続行

            Vec3d explosionOrigin = source.getPosition();
            // 爆発の中心が特定できない場合は、保護せずそのままダメージを適用
            if (explosionOrigin == null) {
                return;
            }

            // --- 被ダメージエンティティがバブルシールド内にいるか確認 ---
            List<BubbleShieldEntity> nearbyShields = world.getEntitiesByType(
                    ModEntities.BUBBLE_SHIELD,
                    self.getBoundingBox().expand(0.5),
                    (shield) -> shield.getAnimatedScale(0.0f) >= 1.0f
            );

            // 被ダメージエンティティがどのシールド内にもいない場合は、保護の対象外
            if (nearbyShields.isEmpty()) {
                return;
            }

            // --- 爆発の中心がシールドの内側にあるシールドが存在するかをチェック ---
            // 「爆発がシールド内なら通常通りの処理にしたい」ロジック
            boolean explosionInsideAnyShield = false;
            for (BubbleShieldEntity shield : nearbyShields) {
                if (shield.getBoundingBox().expand(-1.0).contains(explosionOrigin)) {
                    explosionInsideAnyShield = true;
                    break;
                }
            }

            // --- 保護ロジックの適用 ---
            if (!explosionInsideAnyShield) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}