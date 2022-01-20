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

    @Override
    public void onInitializeClient() {
        System.out.println("Client Initialized");
    }
}
