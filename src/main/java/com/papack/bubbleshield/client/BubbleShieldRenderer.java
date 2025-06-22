package com.papack.bubbleshield.client;

import com.papack.bubbleshield.BubbleShieldEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BubbleShieldRenderer extends EntityRenderer<BubbleShieldEntity> {

    private static final Identifier TEXTURE = Identifier.of("bubbleshield", "textures/entity/bubble_shield.png");

    private static final float SHIELD_SIZE = 4.0f;
    private static final float THICKNESS = 0.05f;

    private static final float MIN_COORD = -SHIELD_SIZE / 2.0f; // -2.0f
    private static final float MAX_COORD = SHIELD_SIZE / 2.0f;  // 2.0f

    private static final float U_MIN = 0.0f;
    private static final float V_MIN = 0.0f;
    private static final float U_MAX = 1.0f;
    private static final float V_MAX = 1.0f;

    int fullBrightLight = 0xF000F0;

    public BubbleShieldRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(BubbleShieldEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        float currentScale = entity.getAnimatedScale(tickDelta);

        matrices.push();
        matrices.translate(0, 1.0f, 0); // 中央に合わせる
        matrices.scale(currentScale, currentScale, currentScale);

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();

        // RenderLayer.getEntityTranslucentEmissive を使用
        VertexConsumer vc = vertexConsumers.getBuffer(BUBBLE_LAYER);


        // 1. Front (北) 面: Z = MIN_COORD - THICKNESS (手前側)
        // 表（正しい法線と反時計回り）
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        // 裏（法線を反転させ、頂点順序も反転）
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD, MIN_COORD - THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);


        // 2. Back (南) 面: Z = MAX_COORD + THICKNESS (奥側)
        // 表（正しい法線と反時計回り）
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, 1.0f);
        // 裏（法線を反転させ、頂点順序も反転）
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD, MAX_COORD + THICKNESS).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 0.0f, -1.0f);


        // 3. Left (西) 面: X = MIN_COORD - THICKNESS (左側)
        // 表（正しい法線 -X と反時計回り）
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MIN_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MAX_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MAX_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MIN_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        // 裏（法線反転 +X と時計回り）
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MIN_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MAX_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MAX_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD - THICKNESS, MIN_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);


        // 4. Right (東) 面: X = MAX_COORD + THICKNESS (右側)
        // 表（正しい法線 +X と反時計回り）
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MIN_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MAX_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MAX_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MIN_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(1.0f, 0.0f, 0.0f);
        // 裏（法線反転 -X と時計回り）
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MIN_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MAX_COORD, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MAX_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD + THICKNESS, MIN_COORD, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(-1.0f, 0.0f, 0.0f);


        // 5. Top (上) 面: Y = MIN_COORD - THICKNESS (上側)
        // 表（正しい法線 +Y と反時計回り）
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD - THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD - THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD - THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD - THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        // 裏（法線反転 -Y と時計回り）
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD - THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD - THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MIN_COORD - THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD, MIN_COORD - THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);


        // 6. Bottom (下) 面: Y = MAX_COORD + THICKNESS (下側)
        // 表（正しい法線 -Y と反時計回り）
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD + THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD + THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD + THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD + THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, -1.0f, 0.0f);
        // 裏（法線反転 +Y と時計回り）
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD + THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD + THICKNESS, MIN_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MAX).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(positionMatrix, MAX_COORD, MAX_COORD + THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MAX, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);
        vc.vertex(positionMatrix, MIN_COORD, MAX_COORD + THICKNESS, MAX_COORD).color(1.0f, 1.0f, 1.0f, 1.0f).texture(U_MIN, V_MIN).overlay(OverlayTexture.DEFAULT_UV).light(fullBrightLight).normal(0.0f, 1.0f, 0.0f);


        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, fullBrightLight);
    }


    @Override
    public Identifier getTexture(BubbleShieldEntity entity) {
        return TEXTURE;
    }

    private static final RenderLayer BUBBLE_LAYER = RenderLayer.of(
            "bubble_shield_layer",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(TEXTURE, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    //.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                    .cull(RenderPhase.ENABLE_CULLING)
                    //.writeMaskState(RenderPhase.COLOR_MASK)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .build(false)
    );
}