package com.papack.bubbleshield.client;

import com.papack.bubbleshield.ModEntities;
import com.papack.bubbleshield.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

import static net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register;


public class BubbleShieldClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // BubbleShieldEntity の型パラメータを明示的に指定
        register(ModEntities.BUBBLE_SHIELD, BubbleShieldRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.BUBBLE_SHIELD_ITEM, new BubbleShieldItemRenderer());

    }
}