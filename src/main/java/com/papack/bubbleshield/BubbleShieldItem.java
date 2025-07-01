package com.papack.bubbleshield;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BubbleShieldItem extends Item {
    private final BubbleShieldType shieldType;
    private final boolean throwable;

    public BubbleShieldItem(Settings settings, BubbleShieldType type, boolean throwable) {
        super(settings);
        this.shieldType = type;
        this.throwable = throwable;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (!user.getItemCooldownManager().isCoolingDown(this)) {
                boolean isSneaking = user.isSneaking();
                //boolean isHealingItem = ((stack.getItem() instanceof BubbleShieldItem shieldItem) && (shieldItem.shieldType == BubbleShieldType.HEALING));

                switch (shieldType) {

                    case THROWN -> {
                        // 投擲タイプ
                        ThrownBubbleShieldEntity thrown = new ThrownBubbleShieldEntity(world, user);
                        thrown.setItem(stack.copy());
                        thrown.setOwner(user);
                        thrown.setType(BubbleShieldType.THROWN);
                        // 向いている方向に投げる速度を与える
                        thrown.setVelocity(
                                user,
                                user.getPitch(),
                                user.getYaw(),
                                0.0f,     // roll（回転）は使わない
                                1.5f,     // スピード（推奨: 1.5f～2.0f）
                                1.0f      // 精度（0が一番正確）
                        );
                        world.spawnEntity(thrown);
                    }

                    case BASE, HEALING, TELEPORT -> {
                        // 設置タイプ（ベース）
                        BubbleShieldEntity shield = new BubbleShieldEntity(world, user.getX(), user.getY(), user.getZ());
                        shield.setOwner(user.getUuid());
                        shield.setAllowOthers(isSneaking);
                        shield.setType(shieldType);
                        world.spawnEntity(shield);
                    }
                }


                if (!user.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                user.getItemCooldownManager().set(this, 100);
            } else {
                return TypedActionResult.pass(stack);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }

}
