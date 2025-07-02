package com.papack.bubbleshield;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;

import java.util.*;
import java.util.List;

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

    private static boolean teleportPlayerIfValid(BubbleShieldEntity shield, ServerWorld serverWorld, ServerPlayerEntity targetPlayer) {
        UUID ownerUuid = shield.getOwnerUuid();
        if (ownerUuid == null) return false;

        ServerPlayerEntity ownerPlayer = serverWorld.getServer().getPlayerManager().getPlayer(ownerUuid);
        if (ownerPlayer == null) return false;

        TeleportTarget teleportTarget = ownerPlayer.getRespawnTarget(true, TeleportTarget.NO_OP);

        Vec3d targetPos;
        ServerWorld targetWorld;

        if (teleportTarget != null && !teleportTarget.missingRespawnBlock()) {
            targetPos = teleportTarget.pos();
            targetWorld = teleportTarget.world();
        } else {
            ServerWorld overworld = serverWorld.getServer().getOverworld();
            BlockPos spawn = overworld.getSpawnPos();
            int y = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING, spawn.getX(), spawn.getZ());
            targetPos = new Vec3d(spawn.getX() + 0.5, y, spawn.getZ() + 0.5);
            targetWorld = overworld;
        }

        if (shield.getBoundingBox().contains(targetPos)) return false;

        targetPlayer.teleport(targetWorld, targetPos.x, targetPos.y, targetPos.z, targetPlayer.getYaw(), targetPlayer.getPitch());
        return true;
    }
}
