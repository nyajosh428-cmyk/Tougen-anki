package com.tougen;

import com.tougen.gun.GunSpec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TougenMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tougen"))
                    .icon(() -> new ItemStack(ModItems.GUNS.get(GunSpec.AK47).get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.BULLET.get());
                        for (GunSpec s : GunSpec.values()) out.accept(ModItems.GUNS.get(s).get());
                    })
                    .build());
}
