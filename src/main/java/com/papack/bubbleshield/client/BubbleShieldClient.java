package com.papack.bubbleshield.client;

import com.papack.bubbleshield.ModEntities;
import com.papack.bubbleshield.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

import static net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register;


public class BubbleShieldClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // BubbleShieldEntity の型パラメータを明示的に指定
        register(ModEntities.BUBBLE_SHIELD, BubbleShieldRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.BASE_SHIELD_ITEM, new BubbleShieldItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.HEALING_SHIELD_ITEM, new BubbleShieldItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.THROWABLE_SHIELD_ITEM, new BubbleShieldItemRenderer());

        register(
                ModEntities.THROWN_BUBBLE_SHIELD,
                FlyingItemEntityRenderer::new
        );

        register(
                ModEntities.BUBBLE_SHIELD,
                BubbleShieldRenderer::new
        );

    }
}