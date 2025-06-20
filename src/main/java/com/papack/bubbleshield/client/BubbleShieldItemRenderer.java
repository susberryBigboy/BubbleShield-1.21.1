package com.papack.bubbleshield.client;

// BubbleShieldEntityが必要
// あなたのアイテムクラス

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

// これを EntityRenderer の ModelPart を再利用するために使います
public class BubbleShieldItemRenderer implements DynamicItemRenderer {

    // BubbleShieldRenderer の ModelPart をここで使えるようにします
    // これを安全に取得するために、BubbleShieldRenderer に static getter を追加するか、
    // ModInitializer で一度だけインスタンス化して保持する必要があります。
    // 今回は仮に、BubbleShieldRenderer と同じモデルをここで再構築します。
    // より良い方法は、BubbleShieldRenderer の ModelPart を再利用することです。
    // (例: private static ModelPart SHIELD_MODEL;)

    private final ModelPart itemCube;
    private static final Identifier TEXTURE = Identifier.of("bubbleshield", "textures/entity/bubble_shield.png");

    public BubbleShieldItemRenderer() {
        // BubbleShieldRenderer と同じ ModelPart 構築ロジックをここにコピーするか、
        // BubbleShieldRenderer から ModelPart を取得する static メソッドを作る
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        int texW = 16;
        int texH = 16;

        root.addChild("front", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f, -0.01f, 16.0f, 16.0f, 0.02f),
                ModelTransform.pivot(0.0f, 0.0f, -8.0f));
        root.addChild("back", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -8.0f, -0.01f, 16.0f, 16.0f, 0.02f),
                ModelTransform.pivot(0.0f, 0.0f, 8.0f));
        root.addChild("left", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-0.01f, -8.0f, -8.0f, 0.02f, 16.0f, 16.0f),
                ModelTransform.pivot(-8.0f, 0.0f, 0.0f));
        root.addChild("right", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-0.01f, -8.0f, -8.0f, 0.02f, 16.0f, 16.0f),
                ModelTransform.pivot(8.0f, 0.0f, 0.0f));
        root.addChild("top", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -0.01f, -8.0f, 16.0f, 0.02f, 16.0f),
                ModelTransform.pivot(0.0f, -8.0f, 0.0f));
        root.addChild("bottom", ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0f, -0.01f, -8.0f, 16.0f, 0.02f, 16.0f),
                ModelTransform.pivot(0.0f, 8.0f, 0.0f));

        TexturedModelData texturedModelData = TexturedModelData.of(modelData, texW, texH);
        this.itemCube = texturedModelData.createModel();
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        matrices.push();

        // アイテムのレンダリングは通常、エンティティよりも小さいスケールで行う必要があります。
        // ここでサイズと位置を調整して、アイテムが適切に見えるようにします。
        // これらは Trial & Error で調整してください
        matrices.translate(0.5, 0.5, 0.5); // アイテムの中心に合わせる（0,0,0が基準点なので）
        matrices.scale(0.05f, 0.05f, 0.05f); // 非常に小さくスケール

        // GUIや三人称視点での追加の回転や平行移動が必要な場合があります
        // mode に応じて matrices を操作することも可能です。
        // if (mode == ModelTransformationMode.GUI) {
        //     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(225));
        //     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        // }

        itemCube.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE)), light, overlay);

        matrices.pop();
    }
}
