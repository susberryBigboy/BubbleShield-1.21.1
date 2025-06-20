package com.papack.bubbleshield;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BubbleShieldItem extends Item {
    public BubbleShieldItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand); // 使用しているItemStackを取得

        // クライアント側ではクールダウンや消費の処理はしない
        if (!world.isClient) {
            // クールダウン中ではないかチェック
            if (!user.getItemCooldownManager().isCoolingDown(this)) {
                Vec3d pos = user.getPos();

                BubbleShieldEntity shield = new BubbleShieldEntity(world, pos.x, pos.y, pos.z);
                shield.setOwner(user.getUuid()); // 所有者を登録
                world.spawnEntity(shield);

                // アイテムを1つ減らす
                if (!user.getAbilities().creativeMode) { // クリエイティブモードではない場合のみ消費
                    itemStack.decrement(1);
                }

                // 5秒間のクールダウンを追加 (100ティック = 5秒)
                user.getItemCooldownManager().set(this, 100);
            } else {
                // クールダウン中の場合は何もせず、使用をキャンセル
                return TypedActionResult.pass(itemStack);
            }
        }

        // 使用が成功した場合は、サーバー側とクライアント側で結果を返す
        return TypedActionResult.success(itemStack, world.isClient());
    }
}

