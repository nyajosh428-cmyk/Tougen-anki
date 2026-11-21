package com.tougen.world;

import com.tougen.net.Net;
import com.tougen.net.RainPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

/** Tracks which dimensions currently have a blood rain (server side). */
public class BloodRain {
    public static final int DURATION_TICKS = 20 * 30; // 30 seconds

    private static final Map<ResourceKey<Level>, Long> END = new HashMap<>();

    public static void start(ServerLevel level) {
        END.put(level.dimension(), level.getGameTime() + DURATION_TICKS);
        // real rain so the vanilla rain sound/visuals run; the client tints it red
        level.setWeatherParameters(0, DURATION_TICKS, true, false);
        Net.CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), new RainPacket(DURATION_TICKS));
    }

    public static void syncTo(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        Long end = END.get(level.dimension());
        int left = end == null ? 0 : (int) Math.max(0, end - level.getGameTime());
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new RainPacket(left));
    }
}
