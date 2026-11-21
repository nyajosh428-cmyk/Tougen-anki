package com.tougen.net;

import com.tougen.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: class, blood %, rank, selected ability, active gun kind, wall-walk ticks left. */
public class SyncPacket {
    private final int classId;
    private final float blood;
    private final int rank;
    private final int selected;
    private final int gunKind;
    private final int legTicks;

    public SyncPacket(int classId, float blood, int rank, int selected, int gunKind, int legTicks) {
        this.classId = classId;
        this.blood = blood;
        this.rank = rank;
        this.selected = selected;
        this.gunKind = gunKind;
        this.legTicks = legTicks;
    }

    public static void encode(SyncPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.classId);
        buf.writeFloat(p.blood);
        buf.writeVarInt(p.rank);
        buf.writeVarInt(p.selected);
        buf.writeVarInt(p.gunKind);
        buf.writeVarInt(p.legTicks);
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        return new SyncPacket(buf.readVarInt(), buf.readFloat(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHooks.onSync(p.classId, p.blood, p.rank, p.selected, p.gunKind, p.legTicks)));
        ctx.get().setPacketHandled(true);
    }
}
