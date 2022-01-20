package com.cinematic.cinematic;

import com.cinematic.cinematic.events.EatEventCallback;
import com.cinematic.cinematic.events.FallEventCallback;
import com.cinematic.cinematic.events.PlayerTickCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;

import java.util.List;
import java.util.Objects;

public class Cinematic implements ModInitializer {

    private boolean waterStatus, fireStatus;
    private static final int DAMAGE_CUTOFF = 5; // 2.5 hearts of fall damage or more

    @Override
    public void onInitialize() {
        System.out.println("Mod Initialized");

        EatEventCallback.EVENT.register(((player, itemstack) -> {

            if (isValidPlayer(player)) {
                fireEvent(EventType.EAT);
            }

            System.out.printf("%s ate %s%n", player.getName().asString(), itemstack.getItem().getName().asString());

            return ActionResult.PASS;
        }));

        PlayerTickCallback.EVENT.register(((player) -> {

            if (isValidPlayer(player)) {
                boolean tmp = player.isTouchingWaterOrRain();
                if (tmp != waterStatus) {
                    waterStatus = tmp;
                    System.out.printf("%s %s water.%n%n", player.getName().asString(), waterStatus ? "left" : "entered");

                    fireEvent(EventType.WATER);
                }

                tmp = player.isOnFire();
                if (tmp != fireStatus) {
                    fireStatus = tmp;
                    System.out.printf("%s %s burning.%n%n", player.getName().asString(), fireStatus ? "started" : "stopped");

                    fireEvent(EventType.FIRE);
                }
            }
            return ActionResult.PASS;
        }));

        FallEventCallback.EVENT.register((player, fallDistance, damageMultiplier, damage) -> {

            System.out.printf("%s fell for %d damage%n", player.getName().asString(), damage);

            if (damage > DAMAGE_CUTOFF && isValidPlayer(player)) {
                fireEvent(EventType.FALL);
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            if (players.stream().noneMatch(this::isValidPlayer)) {
                if (waterStatus) {
                    waterStatus = false;
                    fireEvent(EventType.WATER);
                }

                if (fireStatus) {
                    fireStatus = false;
                    fireEvent(EventType.FIRE);
                }
            }
        });
    }

    private boolean isValidPlayer(PlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return false;

        ServerPlayerEntity sp = server.getPlayerManager().getPlayer(p.getUuid());
        if (sp == null) return false;

        return sp.getScoreboardTags().contains("water") && sp.interactionManager.getGameMode() == GameMode.SURVIVAL;
    }

    private void fireEvent(EventType type) {
        switch (type) {
            case WATER:
                if (waterStatus) {
                    System.out.println("Pouring water...");
                } else {
                    System.out.println("Stopped pouring.");
                }
                break;
            case EAT:
                System.out.println("Please eat now.");
                break;
            case FIRE:
                if (fireStatus) {
                    System.out.println("Pouring hot sauce...");
                } else {
                    System.out.println("Stopped pouring.");
                }
                break;
            case FALL:
                System.out.println("Big Fall Detected.");
                break;
        }
    }
}
