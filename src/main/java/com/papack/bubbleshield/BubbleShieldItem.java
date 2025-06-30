package com.papack.bubbleshield;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
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
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (!user.getItemCooldownManager().isCoolingDown(this)) {
                boolean allowOthers = user.isSneaking();

                if (throwable) {
                    // 投擲型バリア（まだ ThrownBubbleShieldEntity が必要）
                    ThrownBubbleShieldEntity thrown = new ThrownBubbleShieldEntity(world, user);
                    thrown.setOwner(user);
                    thrown.setOwnerSneaking(user.isSneaking());
                    thrown.setType(BubbleShieldType.THROWN); // タイプも渡す
                    world.spawnEntity(thrown);
                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5f, 1.0f);
                } else {
                    // その場設置型バリア
                    Vec3d pos = user.getPos();
                    BubbleShieldEntity shield = new BubbleShieldEntity(world, pos.x, pos.y, pos.z);
                    shield.setOwner(user.getUuid());
                    shield.setAllowOthers(allowOthers);
                    shield.setType(shieldType);
                    world.spawnEntity(shield);
                }

                if (!user.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
                user.getItemCooldownManager().set(this, 100);
            } else {
                return TypedActionResult.pass(itemStack);
            }
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}
