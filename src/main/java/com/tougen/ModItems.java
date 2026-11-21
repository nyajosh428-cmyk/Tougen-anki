package com.tougen;

import com.tougen.gun.GunItem;
import com.tougen.gun.GunSpec;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TougenMod.MODID);

    /** One universal bullet that every gun uses. */
    public static final RegistryObject<Item> BULLET =
            ITEMS.register("bullet", () -> new Item(new Item.Properties()));

    public static final Map<GunSpec, RegistryObject<Item>> GUNS = new EnumMap<>(GunSpec.class);

    static {
        for (GunSpec spec : GunSpec.values()) {
            GUNS.put(spec, ITEMS.register(spec.id, () -> new GunItem(spec)));
        }
    }
}
