package com.tougen.net;

import com.tougen.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: blood rain for this many more ticks (0 = stop). */
public class RainPacket {
    private final int ticks;

    public RainPacket(int ticks) {
        this.ticks = ticks;
    }

    public static void encode(RainPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.ticks);
    }

    public static RainPacket decode(FriendlyByteBuf buf) {
        return new RainPacket(buf.readVarInt());
    }

    public static void handle(RainPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHooks.onRain(p.ticks)));
        ctx.get().setPacketHandled(true);
    }
}
