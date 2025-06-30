package com.papack.bubbleshield;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("deprecation")
public class ModEntities {

    public static final EntityType<BubbleShieldEntity> BUBBLE_SHIELD =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(Bubbleshield.MOD_ID, "bubble_shield"),
                    FabricEntityTypeBuilder.<BubbleShieldEntity>create(SpawnGroup.MISC, BubbleShieldEntity::new)
                            .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                            .trackRangeBlocks(10)
                            .trackedUpdateRate(20)
                            .build()
            );

    public static final EntityType<ThrownBubbleShieldEntity> THROWN_BUBBLE_SHIELD =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(Bubbleshield.MOD_ID, "thrown_bubble_shield"),
                    FabricEntityTypeBuilder.<ThrownBubbleShieldEntity>create(SpawnGroup.MISC, ThrownBubbleShieldEntity::new)
                            .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                            .trackRangeBlocks(4)
                            .trackedUpdateRate(10)
                            .build()
            );

    public static void register() {
    }
}
