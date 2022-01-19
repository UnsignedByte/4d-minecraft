package com.cinematic.cinematic.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public interface EatEventCallback {

    Event<EatEventCallback> EVENT = EventFactory.createArrayBacked(EatEventCallback.class, (listeners) -> (player, itemstack) -> {
        for (EatEventCallback listener : listeners) {
            ActionResult result = listener.interact(player, itemstack);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult interact(PlayerEntity player, ItemStack itemstack);
}
