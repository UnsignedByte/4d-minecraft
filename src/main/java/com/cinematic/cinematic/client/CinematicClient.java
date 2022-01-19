package com.cinematic.cinematic.client;

import com.cinematic.cinematic.EventType;
import com.cinematic.cinematic.events.EatEventCallback;
import com.cinematic.cinematic.events.FallEventCallback;
import com.cinematic.cinematic.events.PlayerTickCallback;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class CinematicClient implements ClientModInitializer {
    private boolean waterStatus, fireStatus;
    private static final int DAMAGE_CUTOFF = 5; // 2.5 hearts of fall damage or more

    @Override
    public void onInitializeClient() {
        System.out.println("Client Initialized");

        EatEventCallback.EVENT.register(((player, itemstack) -> {

            fireEvent(player, EventType.EAT);

            System.out.printf("%s ate %s%n", player.getName().asString(), itemstack.getItem().getName().asString());

            return ActionResult.PASS;
        }));

        PlayerTickCallback.EVENT.register(((player) -> {

            if (isValidPlayer(player)) {
                boolean tmp = player.isTouchingWaterOrRain();
                if (tmp != waterStatus) {
                    waterStatus = tmp;
                    System.out.printf("%s %s water.%n%n", player.getName().asString(), waterStatus ? "left" : "entered");

                    fireEvent(player, EventType.WATER);
                }

                tmp = player.isOnFire();
                if (tmp != fireStatus) {
                    fireStatus = tmp;
                    System.out.printf("%s %s burning.%n%n", player.getName().asString(), fireStatus ? "started" : "stopped");

                    fireEvent(player, EventType.FIRE);
                }
            }
            return ActionResult.PASS;
        }));

        FallEventCallback.EVENT.register((player, fallDistance, damageMultiplier, damage) -> {

            System.out.printf("%s fell for %d damage%n", player.getName().asString(), damage);

            if (damage > DAMAGE_CUTOFF) {
                fireEvent(player, EventType.FALL);
            }

            return ActionResult.PASS;
        });
    }

    private boolean isValidPlayer(PlayerEntity p) {
        return p.getScoreboardTags().contains("water");
    }

    private void fireEvent(PlayerEntity p, EventType type) {
        if (isValidPlayer(p)) {
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
}
