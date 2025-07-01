package com.papack.bubbleshield.client;

import com.papack.bubbleshield.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

import static net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register;


public class BubbleShieldClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        register(ModEntities.BUBBLE_SHIELD, BubbleShieldRenderer::new);

        register(ModEntities.THROWN_BUBBLE_SHIELD, FlyingItemEntityRenderer::new
        );
    }
}