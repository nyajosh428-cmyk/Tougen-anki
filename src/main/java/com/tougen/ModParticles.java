package com.tougen;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TougenMod.MODID);

    /** Falling red streak used for blood rain and for the dripping Shiki gun. */
    public static final RegistryObject<SimpleParticleType> BLOOD_DROP =
            PARTICLES.register("blood_drop", () -> new SimpleParticleType(false));
}
