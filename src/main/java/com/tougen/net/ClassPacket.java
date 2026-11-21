package com.tougen.net;

import com.tougen.ServerEvents;
import com.tougen.data.OniData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server: the player picked Oni or Momotaro. */
public class ClassPacket {
    private final int classId;

    public ClassPacket(int classId) {
        this.classId = classId;
    }

    public static void encode(ClassPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.classId);
    }

    public static ClassPacket decode(FriendlyByteBuf buf) {
        return new ClassPacket(buf.readVarInt());
    }

    public static void handle(ClassPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            if (p.classId != OniData.ONI && p.classId != OniData.MOMOTARO) return;
            OniData.setClassId(sp, p.classId);
            String msg = p.classId == OniData.ONI
                    ? "You are an ONI. V = summon ability, B = switch ability."
                    : "You are a MOMOTARO.";
            sp.displayClientMessage(Component.literal(msg), true);
            ServerEvents.sync(sp);
        });
        ctx.get().setPacketHandled(true);
    }
}
