package com.papack.bubbleshield;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

@SuppressWarnings("deprecation")
public class ModEntities {

    // Change this line:
    public static final EntityType<BubbleShieldEntity> BUBBLE_SHIELD = FabricEntityTypeBuilder
            .<BubbleShieldEntity>create(SpawnGroup.MISC, BubbleShieldEntity::new) // Add <BubbleShieldEntity> here
            .dimensions(EntityDimensions.fixed(3.0f, 0.0f))
            .trackRangeBlocks(64)
            .trackedUpdateRate(1)
            .build();


    public static void initialize() {
        Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of("bubbleshield", "bubble_shield"),
                BUBBLE_SHIELD
        );
    }
}

