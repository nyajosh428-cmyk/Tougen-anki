package com.tougen.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tougen.ModEntities;
import com.tougen.ModParticles;
import com.tougen.TougenMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TougenMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    public static final String CATEGORY = "key.categories.tougen";

    public static final KeyMapping CLASS_KEY = new KeyMapping("key.tougen.class_menu",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping RELOAD_KEY = new KeyMapping("key.tougen.reload",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping BIG_GUN_KEY = new KeyMapping("key.tougen.big_gun",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    public static final KeyMapping CYCLE_KEY = new KeyMapping("key.tougen.cycle",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(CYCLE_KEY);
        e.register(CLASS_KEY);
        e.register(RELOAD_KEY);
        e.register(BIG_GUN_KEY);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.SHIKI_GUN.get(), ShikiGunRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(ShikiGunRenderer.LAYER, ShikiGunModel::createLayer);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("hud", new BloodMeterOverlay());
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(ModParticles.BLOOD_DROP.get(), BloodDropParticle.Provider::new);
    }
}
