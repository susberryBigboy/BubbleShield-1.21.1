package com.papack.bubbleshield.client;

import com.papack.bubbleshield.BubbleShieldEntity;
import com.papack.bubbleshield.Bubbleshield;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BubbleShieldRenderer extends EntityRenderer<BubbleShieldEntity> {

    private static final Identifier TEXTURE_BASE = Identifier.of(Bubbleshield.MOD_ID, "textures/entity/bubble_shield.png");
    private static final Identifier TEXTURE_HEAL = Identifier.of(Bubbleshield.MOD_ID, "textures/entity/bubble_shield_heal.png");
    private static final Identifier TEXTURE_TELEPORT = Identifier.of(Bubbleshield.MOD_ID, "textures/entity/bubble_shield_teleport.png");
    private static final Identifier TEXTURE_THROWABLE = Identifier.of(Bubbleshield.MOD_ID, "textures/entity/bubble_shield_throwable.png");
    private final ModelPart cube;

    public BubbleShieldRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);

        // ① ModelData を作成
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        int texW = 16;
        int texH = 16;

        // ② 各面を追加（6枚の板）
        // 上面
        root.addChild("top", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -0.01f, -8.0f, 16.0f, 0.02f, 16.0f),
                ModelTransform.of(0.0f, 8.0f, 0.0f, (float) Math.PI, 0.0f, 0.0f));

        // 下面
        root.addChild("bottom", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -0.01f, -8.0f, 16.0f, 0.02f, 16.0f),
                ModelTransform.of(0.0f, -8.0f, 0.0f, 0.0f, 0.0f, 0.0f));

        // front
        root.addChild("front", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f, -0.01f, 16.0f, 16.0f, 0.02f),
                ModelTransform.of(0.0f, 0.0f, -8.0f, (float) Math.PI, 0.0f, 0.0f));

        // back
        root.addChild("back", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f, -0.01f, 16.0f, 16.0f, 0.02f),
                ModelTransform.of(0.0f, 0.0f, 8.0f, (float) Math.PI, 0.0f, 0.0f));

        // left
        root.addChild("left", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-0.01f, -8.0f, -8.0f, 0.02f, 16.0f, 16.0f),
                ModelTransform.of(-8.0f, 0.0f, 0.0f, (float) Math.PI, 0.0f, 0.0f));

        // right
        root.addChild("right", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-0.01f, -8.0f, -8.0f, 0.02f, 16.0f, 16.0f),
                ModelTransform.of(8.0f, 0.0f, 0.0f, (float) Math.PI, 0.0f, 0.0f));


        // ③ TexturedModelData にして ModelPart を生成
        TexturedModelData texturedModelData = TexturedModelData.of(modelData, texW, texH);
        this.cube = texturedModelData.createModel();
    }

    @Override
    public void render(BubbleShieldEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        float progress = entity.getAnimatedScale(tickDelta);
        float scale = 4.0f * progress;

        matrices.push();
        matrices.translate(0, 1.0f, 0);
        matrices.scale(scale, scale, scale);

        // テレポートタイプかどうかでレイヤー切り替え
        VertexConsumer vc;
        switch (entity.getTypeEnum()) {
            // case TELEPORT -> vc = vertexConsumers.getBuffer(RenderLayer.getEndPortal());  Shaders Pack を使うと表示されなくなる
            case TELEPORT -> vc = vertexConsumers.getBuffer(BUBBLE_LAYER_TELEPORT);
            case THROWN -> vc = vertexConsumers.getBuffer(BUBBLE_LAYER_THROWABLE);
            case HEALING -> vc = vertexConsumers.getBuffer(BUBBLE_LAYER_HEAL);
            default -> vc = vertexConsumers.getBuffer(BUBBLE_LAYER);
        }

        cube.render(matrices, vc, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }


    @Override
    public Identifier getTexture(BubbleShieldEntity entity) {
        return switch (entity.getTypeEnum()) {
            case TELEPORT -> TEXTURE_TELEPORT;
            case THROWN -> TEXTURE_THROWABLE;
            case HEALING -> TEXTURE_HEAL;
            default -> TEXTURE_BASE;
        };
    }

    private static final RenderLayer BUBBLE_LAYER = RenderLayer.of(
            "bubble_shield_layer",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(TEXTURE_BASE, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.ENABLE_CULLING)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .build(false)
    );

    private static final RenderLayer BUBBLE_LAYER_HEAL = RenderLayer.of(
            "bubble_shield_layer",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(TEXTURE_HEAL, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.ENABLE_CULLING)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .build(false)
    );

    private static final RenderLayer BUBBLE_LAYER_THROWABLE = RenderLayer.of(
            "bubble_shield_layer",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(TEXTURE_THROWABLE, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.ENABLE_CULLING)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .build(false)
    );

    private static final RenderLayer BUBBLE_LAYER_TELEPORT = RenderLayer.of(
            "bubble_shield_layer",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            1536,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM)
                    .texture(new RenderPhase.Texture(TEXTURE_TELEPORT, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .cull(RenderPhase.ENABLE_CULLING)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .target(RenderPhase.ITEM_ENTITY_TARGET)
                    .build(false)
    );
}
