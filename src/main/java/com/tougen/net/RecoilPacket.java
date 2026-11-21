package com.tougen.net;

import com.tougen.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> shooter: kick the camera. */
public class RecoilPacket {
    private final float pitch;
    private final float yaw;

    public RecoilPacket(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public static void encode(RecoilPacket p, FriendlyByteBuf buf) {
        buf.writeFloat(p.pitch);
        buf.writeFloat(p.yaw);
    }

    public static RecoilPacket decode(FriendlyByteBuf buf) {
        return new RecoilPacket(buf.readFloat(), buf.readFloat());
    }

    public static void handle(RecoilPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHooks.onRecoil(p.pitch, p.yaw)));
        ctx.get().setPacketHandled(true);
    }
}
