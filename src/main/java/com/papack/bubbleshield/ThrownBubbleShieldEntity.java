package com.papack.bubbleshield;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class ThrownBubbleShieldEntity extends ThrownItemEntity {
    private boolean ownerSneaking = false;
    BubbleShieldType type;

    public ThrownBubbleShieldEntity(World world, LivingEntity owner) {
        super(ModEntities.THROWN_BUBBLE_SHIELD, owner, world);
        this.setOwnerSneaking(owner.isSneaking());
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (!this.getWorld().isClient && this.getOwner() != null) {
            BubbleShieldEntity shield = new BubbleShieldEntity(this.getWorld(), getX(), getY(), getZ());
            shield.setOwner(this.getOwner().getUuid());
            shield.setAllowOthers(ownerIsSneaking());
            shield.setType(BubbleShieldType.THROWN);
            this.getWorld().spawnEntity(shield);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }

    public void setOwnerSneaking(boolean sneaking) {
        this.ownerSneaking = sneaking;
    }

    public boolean ownerIsSneaking() {
        return this.ownerSneaking;
    }

    public void setType(BubbleShieldType type) {
        this.type = type;
    }

    public BubbleShieldType getType() {
        return type;
    }
}

