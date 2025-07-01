package com.papack.bubbleshield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class TeleportHandler {

    public static final int TELEPORT_RADIUS = 4;
    private static final Set<UUID> teleportedPlayers = new HashSet<>();

    public static void tryTeleportOwnerOnly(BubbleShieldEntity shield) {
        if (!(shield.getWorld() instanceof ServerWorld serverWorld)) return;

        UUID ownerUuid = shield.getOwnerUuid();
        if (ownerUuid == null) return;

        Entity entity = serverWorld.getEntity(ownerUuid);
        if (!(entity instanceof ServerPlayerEntity player)) return;

        teleportPlayerIfValid(shield, serverWorld, player);
    }

    public static void tryTeleportOthers(BubbleShieldEntity shield) {
        if (!(shield.getWorld() instanceof ServerWorld serverWorld)) return;

        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class,
                shield.getBoundingBox().expand(TELEPORT_RADIUS),
                entity -> entity instanceof PlayerEntity);

        for (Entity entity : targets) {
            if (!(entity instanceof ServerPlayerEntity player)) continue;
            if (shield.isOwner(player.getUuid())) continue;
            if (!shield.allowsOthers()) continue;

            if (teleportedPlayers.contains(player.getUuid())) continue;

            if (teleportPlayerIfValid(shield, serverWorld, player)) {
                teleportedPlayers.add(player.getUuid());
            }
        }
    }

    private static boolean teleportPlayerIfValid(BubbleShieldEntity shield, ServerWorld serverWorld, ServerPlayerEntity player) {
        BlockPos respawnPos = player.getSpawnPointPosition();
        ServerWorld respawnWorld = serverWorld;

        if (player.getSpawnPointDimension() != null) {
            ServerWorld maybeWorld = serverWorld.getServer().getWorld(player.getSpawnPointDimension());
            if (maybeWorld != null) {
                respawnWorld = maybeWorld;
            }
        }

        if (respawnPos == null || !respawnWorld.getBlockState(respawnPos).isOpaque()) {
            respawnWorld = serverWorld.getServer().getOverworld();
            respawnPos = respawnWorld.getSpawnPos();
        }

        Vec3d targetPos = new Vec3d(respawnPos.getX() + 0.5, respawnPos.getY(), respawnPos.getZ() + 0.5);

        if (shield.getBoundingBox().contains(targetPos)) return false;

        player.teleport(respawnWorld, targetPos.x, targetPos.y, targetPos.z, player.getYaw(), player.getPitch());
        return true;
    }
}
