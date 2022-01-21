package com.cinematic.cinematic;

import com.cinematic.cinematic.events.EatEventCallback;
import com.cinematic.cinematic.events.FallEventCallback;
import com.cinematic.cinematic.events.PlayerTickCallback;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Cinematic implements ModInitializer {

    private boolean waterStatus, fireStatus;
    private static final int DAMAGE_CUTOFF = 5; // 2.5 hearts of fall damage or more

    private boolean connected = false;
    private OutputStream arduino;

    @Override
    public void onInitialize() {

        // Initialize arduino serial port
        SerialPort port = SerialPort.getCommPort("COM5");
        port.setComPortParameters(9600, 8, 1, 0);
        port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        System.out.printf("Open port: %b%n", port.openPort());

        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                System.out.printf("Received %s%n", new String(event.getReceivedData()));
                if (java.util.Arrays.equals(event.getReceivedData(),"ready".getBytes(StandardCharsets.UTF_8))) {
                    System.out.println("Received ready.");
                    connected = true;
                }
            }
        });

        arduino = port.getOutputStream();

        System.out.println("Ready sent, awaiting arduino response...");
        while (!sendEvent(ArduinoEvent.READY)) {
            try {
                Thread.sleep(1_000);
                System.out.println("Ready not received, trying again.");
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted:");
                e.printStackTrace();
            }
        };


        // Initialize event listeners

        EatEventCallback.EVENT.register(((player, itemstack) -> {

            if (isValidPlayer(player)) {
                fireEvent(EventType.EAT);

                System.out.printf("%s ate %s%n", player.getName().asString(), itemstack.getItem().getName().asString());
            }

            return ActionResult.PASS;
        }));

        PlayerTickCallback.EVENT.register(((player) -> {

            if (isValidPlayer(player)) {
                boolean tmp = player.isTouchingWaterOrRain();
                if (tmp != waterStatus) {
                    waterStatus = tmp;
                    System.out.printf("%s %s water.%n%n", player.getName().asString(), waterStatus ? "left" : "entered");

                    /*
                    W | R | W
                    0 | 1 | 0
                    1 | 1 | 1
                    1 | 0 | 0
                    0 | 0 | 1
                     */

                    waterStatus ^= !fireEvent(EventType.WATER);
                }

                tmp = player.isOnFire();
                if (tmp != fireStatus) {
                    fireStatus = tmp;
                    System.out.printf("%s %s burning.%n%n", player.getName().asString(), fireStatus ? "started" : "stopped");

                    fireStatus ^= !fireEvent(EventType.FIRE);
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
            if (!connected || players.stream().noneMatch(this::isValidPlayer)) {
                if (waterStatus) {
                    waterStatus = false;
                    waterStatus = !fireEvent(EventType.WATER); // keep at false if success, set back to true if fail
                }

                if (fireStatus) {
                    fireStatus = false;
                    fireStatus = !fireEvent(EventType.FIRE); // keep at false if success, set back to true if fail
                }
            }
        });

        System.out.println("Mod Initialized");
    }

    private boolean isValidPlayer(PlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null) return false;

        ServerPlayerEntity sp = server.getPlayerManager().getPlayer(p.getUuid());
        if (sp == null) return false;

        return sp.getScoreboardTags().contains("water") && sp.interactionManager.getGameMode() == GameMode.SURVIVAL;
    }

    /**
     * Fires an event type to the arduinos
     * @param type The type of event to fire
     * @return Success of message send
     */
    private boolean fireEvent(EventType type) {
        switch (type) {
            case WATER:
                if (waterStatus) {
                    System.out.println("Pouring water...");
                    return sendEvent(ArduinoEvent.WATER_BEGIN);
                } else {
                    System.out.println("Stopped pouring.");
                    return sendEvent(ArduinoEvent.WATER_END);
                }
            case EAT:
                System.out.println("Please eat now.");
                sendEvent(ArduinoEvent.EAT);
            case FIRE:
                if (fireStatus) {
                    System.out.println("Pouring hot sauce...");
                    return sendEvent(ArduinoEvent.FIRE_BEGIN);
                } else {
                    System.out.println("Stopped pouring.");
                    return sendEvent(ArduinoEvent.FIRE_END);
                }
            case FALL:
                System.out.println("Big Fall Detected.");
                return sendEvent(ArduinoEvent.FALL);
        }
        return false;
    }

    /**
     * Sends event to the arduino
     * @param evt event to send
     * @return success of send
     */
    private boolean sendEvent(ArduinoEvent evt) {
        if (arduino == null) {
            System.out.printf("Failed to send event %s: Missing output stream.%n", evt.name());
            return false;
        }
        byte[] buf = {evt.value};
        try {
            arduino.write(buf);
            return true;
        } catch (IOException e) {
            System.out.printf("Failed to send event %s: IO Exception%n", evt.name());
            e.printStackTrace();
            return false;
        }
    }

    private enum ArduinoEvent {
        WATER_BEGIN ((byte) 0),
        WATER_END ((byte) 1),
        FIRE_BEGIN ((byte) 2),
        FIRE_END((byte) 3),
        EAT ((byte) 4),
        FALL ((byte) 5),
        READY ((byte) 7);

        byte value;
        ArduinoEvent(byte value) {
            this.value = value;
        }
    }
}
