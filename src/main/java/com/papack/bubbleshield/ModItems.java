package com.papack.bubbleshield;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item BUBBLE_SHIELD_ITEM;

    static {
        BUBBLE_SHIELD_ITEM = Registry.register(
                Registries.ITEM,
                Identifier.of("bubbleshield", "textures/item/bubble_shield_item"),
                new BubbleShieldItem(new Item.Settings().maxCount(16))
        );
    }

    public static void initialize() {
    }

}