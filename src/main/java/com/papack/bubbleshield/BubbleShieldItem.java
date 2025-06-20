package com.papack.bubbleshield;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BubbleShieldItem extends Item {
    public BubbleShieldItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            Vec3d pos = user.getPos();

            BubbleShieldEntity shield = new BubbleShieldEntity(world, pos.x, pos.y, pos.z);
            shield.setOwner(user.getUuid()); // 所有者を登録
            world.spawnEntity(shield);
        }

        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }
}

