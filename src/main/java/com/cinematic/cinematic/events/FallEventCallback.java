package com.cinematic.cinematic.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface FallEventCallback {

    Event<FallEventCallback> EVENT = EventFactory.createArrayBacked(FallEventCallback.class, (listeners) -> (player, fallDistance, damageMultiplier, damage) -> {
        for (FallEventCallback listener : listeners) {
            ActionResult result = listener.interact(player, fallDistance, damageMultiplier, damage);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult interact(PlayerEntity player, float fallDistance, float damageMultiplier, int damage);
}
