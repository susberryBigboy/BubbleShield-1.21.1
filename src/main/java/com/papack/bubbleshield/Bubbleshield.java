package com.papack.bubbleshield;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;


public class Bubbleshield implements ModInitializer {

    @Override
    public void onInitialize() {

        ModItems.initialize();
        ModEntities.initialize();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(ModItems.BUBBLE_SHIELD_ITEM));
    }
}
