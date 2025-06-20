package com.papack.bubbleshield;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;


public class Bubbleshield implements ModInitializer {

    @Override
    public void onInitialize() {

        ModItems.initialize();
        ModEntities.initialize();


        // クリーパーの爆発ダメージを処理するためのイベントリスナー登録
        ServerLivingEntityEvents.AFTER_DAMAGE.register((livingEntity, damageSource, affectedEntities) -> {
            World world = livingEntity.getWorld();
            if (world.isClient) {
                return;
            }

            List<BubbleShieldEntity> bubbleShields = world.getEntitiesByType(
                    EntityType.get(), // ModEntities.BUBBLE_SHIELD を直接使う
                    new Box(livingEntity.getX() - 10, livingEntity.getY() - 10, livingEntity.getZ() - 10,
                            livingEntity.getX() + 10, livingEntity.getY() + 10, livingEntity.getZ() + 10),
                    (entity) -> true
            );


                for (BubbleShieldEntity shield : bubbleShields) {
                    if (shield.getAnimatedScale(0.0f) >= 1.0f && // シールドが完全に展開しているか
                            shield.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
                        // protectedEntity がシールドの範囲内にいる場合、ダメージを軽減または無効化
                        // ExplosionEvent.DETONATED はダメージを直接キャンセルするわけではないので、
                        // 影響を受けるエンティティリストから削除するか、より低いレベルのイベントで処理が必要です。

                        // --- より確実な方法: LivingEntityDeathCallback / LivingEntityDamageCallback ---
                        // ExplosionEvent.DETONATED は「爆発の影響を受けるエンティティリスト」を操作できるが、
                        // 実際にダメージをキャンセルするには不十分な場合が多いです。
                        // そのため、LivingEntityDamageCallback を使う方が確実です。
                    }
            }
        });
    }
}
