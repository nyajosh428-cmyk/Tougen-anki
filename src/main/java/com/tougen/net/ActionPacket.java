package com.tougen.net;

import com.tougen.entity.ShikiAbility;
import com.tougen.gun.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> server key actions. */
public class ActionPacket {
    public static final byte RELOAD = 0;
    public static final byte ABILITY = 1;   // V: summon selected ability / dismiss the gun
    public static final byte CYCLE = 2;     // B: next ability
    public static final byte FIRE = 3;      // left click with a blood gun
    public static final byte AIM_ON = 4;    // right click held
    public static final byte AIM_OFF = 5;

    private final byte action;

    public ActionPacket(byte action) {
        this.action = action;
    }

    public static void encode(ActionPacket p, FriendlyByteBuf buf) {
        buf.writeByte(p.action);
    }

    public static ActionPacket decode(FriendlyByteBuf buf) {
        return new ActionPacket(buf.readByte());
    }

    public static void handle(ActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            switch (p.action) {
                case RELOAD -> {
                    ItemStack held = sp.getMainHandItem();
                    if (held.getItem() instanceof GunItem gun) gun.startReload(sp, held);
                }
                case ABILITY -> ShikiAbility.use(sp);
                case CYCLE -> ShikiAbility.cycle(sp);
                case FIRE -> ShikiAbility.fire(sp);
                case AIM_ON -> ShikiAbility.aim(sp, true);
                case AIM_OFF -> ShikiAbility.aim(sp, false);
                default -> {
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
