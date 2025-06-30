package com.papack.bubbleshield;

import net.minecraft.item.Item;

import static net.minecraft.item.Items.register;

public class ModItems {

    public static final Item BASE_SHIELD_ITEM = register("bubble_shield_base",
            new BubbleShieldItem(new Item.Settings().maxCount(16), BubbleShieldType.BASE, false));

    public static final Item HEALING_SHIELD_ITEM = register("bubble_shield_heal",
            new BubbleShieldItem(new Item.Settings().maxCount(16), BubbleShieldType.HEALING, false));

    public static final Item THROWABLE_SHIELD_ITEM = register("bubble_shield_throwable",
            new BubbleShieldItem(new Item.Settings().maxCount(16), BubbleShieldType.BASE, true));


    public static void initialize() {
    }

}