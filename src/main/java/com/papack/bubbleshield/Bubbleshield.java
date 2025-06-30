package com.papack.bubbleshield;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;


public class Bubbleshield implements ModInitializer {
    public static String MOD_ID = "bubbleshield";

    @Override
    public void onInitialize() {


        ModItems.initialize();
        ModEntities.register();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(ModItems.BASE_SHIELD_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(ModItems.HEALING_SHIELD_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(ModItems.THROWABLE_SHIELD_ITEM));
    }
}
