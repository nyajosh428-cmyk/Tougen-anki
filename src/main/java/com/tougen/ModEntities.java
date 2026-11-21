package com.tougen;

import com.tougen.entity.ShikiGunEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TougenMod.MODID);

    public static final RegistryObject<EntityType<ShikiGunEntity>> SHIKI_GUN = ENTITIES.register("shiki_gun",
            () -> EntityType.Builder.<ShikiGunEntity>of(ShikiGunEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .fireImmune()
                    .noSave()
                    .build("shiki_gun"));
}
