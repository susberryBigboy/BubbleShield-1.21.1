package com.papack.bubbleshield.client;

import com.papack.bubbleshield.BubbleShieldEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class BubbleShieldRenderer extends EntityRenderer<BubbleShieldEntity> {

    private static final Identifier TEXTURE = Identifier.of("bubbleshield", "textures/entity/bubble_shield.png");
    private final ModelPart cube;

    public BubbleShieldRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);

        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        int texW = 16; // ★もしテクスチャが512x512なら、ここを512にしてください。
        int texH = 16; // ★もしテクスチャが512x512なら、ここを512にしてください。

        // 各面を追加（6枚の板）
        // 各cuboidの始点と厚み、pivotを慎重に設定します。
        // 目標: 各面が、シールドの中心 (0,0,0) から見て外側に薄い板として存在する。

        // front (北) 面: Z軸のマイナス方向。Zの終わりが-8.0fになるように。
        // cuboid の z は、pivot からの相対位置で -8.0f の位置に板の開始が来るように調整
        // 厚み 0.02f で、Z軸のマイナス方向に伸びる板
        root.addChild("front", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f, -8.0f - 0.01f, 16.0f, 16.0f, 0.02f),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f)); // pivot を 0,0,0 にすることで、cuboid の座標がワールド座標と直結する

        // back (南) 面: Z軸のプラス方向。Zの開始が8.0fになるように。
        // 厚み 0.02f で、Z軸のプラス方向に伸びる板
        root.addChild("back", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f, 8.0f - 0.01f, 16.0f, 16.0f, 0.02f), // Zの開始を 8.0f から少し内側へ
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        // left (西) 面: X軸のマイナス方向。Xの終わりが-8.0fになるように。
        // ★修正: cuboid のmirror引数を true に設定
        // 厚み 0.02f で、X軸のマイナス方向に伸びる板
        root.addChild("left", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f - 0.01f, -8.0f, -8.0f, 0.02f, 16.0f, 16.0f, true), // ここを修正 (mirror = true)
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        // right (東) 面: X軸のプラス方向。Xの開始が8.0fになるように。
        // ★修正: cuboid のmirror引数を true に設定
        // 厚み 0.02f で、X軸のプラス方向に伸びる板
        root.addChild("right", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(8.0f - 0.01f, -8.0f, -8.0f, 0.02f, 16.0f, 16.0f, true), // ここを修正 (mirror = true)
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        // top (上) 面: Y軸のマイナス方向。Yの終わりが-8.0fになるように。
        // 厚み 0.02f で、Y軸のマイナス方向に伸びる板
        root.addChild("top", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f - 0.01f, -8.0f, 16.0f, 0.02f, 16.0f),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        // bottom (下) 面: Y軸のプラス方向。Yの開始が8.0fになるように。
        // 厚み 0.02f で、Y軸のプラス方向に伸びる板
        root.addChild("bottom", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, 8.0f - 0.01f, -8.0f, 16.0f, 0.02f, 16.0f),
                ModelTransform.pivot(0.0f, 0.0f, 0.0f));

        TexturedModelData texturedModelData = TexturedModelData.of(modelData, texW, texH);
        this.cube = texturedModelData.createModel();
    }

    @Override
    public void render(BubbleShieldEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        float progress = entity.getAnimatedScale(tickDelta); // 0.0 ～ 1.0
        float scale = 4.0f * progress;

        matrices.push();
        matrices.translate(0, 1.0f, 0); // プレイヤー中心に合わせる (エンティティの足元が0,0,0なので、上に持ち上げる)
        matrices.scale(scale, scale, scale);

        // RenderLayer.getEntityTranslucent はカリングを行いません。
        // そのため、法線が反転していると裏面も描画されてZ-fightingや暗くなる現象が起きます。
        // mirror = true で法線を強制的に反転させることで、この問題を解決できる可能性があります。
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        cube.render(matrices, vc, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(BubbleShieldEntity entity) {
        return TEXTURE;
    }
}