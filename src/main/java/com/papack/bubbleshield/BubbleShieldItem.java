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
                boolean isHealingItem = this == ModItems.BASE_SHIELD_ITEM;
                boolean isThrowable = this == ModItems.THROWABLE_SHIELD_ITEM;

                if (isThrowable || isHealingItem) {
                    // 投擲タイプ
                    ThrownBubbleShieldEntity thrown = new ThrownBubbleShieldEntity(ModEntities.THROWN_BUBBLE_SHIELD, world, user);
                    thrown.setItem(stack);
                    thrown.setOwner(user);
                    thrown.setType(isHealingItem ? BubbleShieldType.HEALING : BubbleShieldType.BASE);
                    thrown.setAllowOthers(isSneaking);
                    world.spawnEntity(thrown);
                } else {
                    // 設置タイプ（ベース）
                    BubbleShieldEntity shield = new BubbleShieldEntity(world, user.getX(), user.getY(), user.getZ());
                    shield.setOwner(user.getUuid());
                    shield.setAllowOthers(isSneaking);
                    shield.setType(BubbleShieldType.BASE);
                    world.spawnEntity(shield);
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
