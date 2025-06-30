package com.papack.bubbleshield;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class ThrownBubbleShieldEntity extends ThrownItemEntity {
    private boolean ownerSneaking = false;

    public ThrownBubbleShieldEntity(EntityType<? extends ThrownBubbleShieldEntity> type, World world) {
        super(type, world);
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
}

