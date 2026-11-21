package com.tougen.net;

import com.tougen.TougenMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Net {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TougenMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle);
        CHANNEL.registerMessage(id++, ClassPacket.class, ClassPacket::encode, ClassPacket::decode, ClassPacket::handle);
        CHANNEL.registerMessage(id++, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
        CHANNEL.registerMessage(id++, RecoilPacket.class, RecoilPacket::encode, RecoilPacket::decode, RecoilPacket::handle);
        CHANNEL.registerMessage(id++, RainPacket.class, RainPacket::encode, RainPacket::decode, RainPacket::handle);
    }
}
