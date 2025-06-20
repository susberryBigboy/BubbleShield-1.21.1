package com.papack.bubbleshield;

import net.fabricmc.api.ModInitializer;

public class Bubbleshield implements ModInitializer {

    @Override
    public void onInitialize() {

        ModItems.initialize();
        ModEntities.initialize();

    }
}
