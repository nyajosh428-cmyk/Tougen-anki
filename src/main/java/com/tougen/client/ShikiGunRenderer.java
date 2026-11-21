package com.tougen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tougen.TougenMod;
import com.tougen.data.OniAbility;
import com.tougen.entity.ShikiGunEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ShikiGunRenderer extends EntityRenderer<ShikiGunEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TougenMod.MODID, "shiki_gun"), "main");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TougenMod.MODID, "textures/entity/shiki_gun.png");

    private final ShikiGunModel model;

    public ShikiGunRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new ShikiGunModel(ctx.bakeLayer(LAYER));
        this.shadowRadius = 0f;
    }

    @Override
    public void render(ShikiGunEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffer, int light) {
        OniAbility k = e.kind();
        float t = e.tickCount + pt;
        float build = Mth.clamp(t / Math.max(1, k.buildTicks), 0f, 1f);

        float alpha = 1f;
        int ds = e.dissolveStart();
        if (ds >= 0) {
            alpha = 1f - Mth.clamp((t - ds) / (float) ShikiGunEntity.DISSOLVE_TICKS, 0f, 1f);
        }
        if (alpha <= 0.01f) return;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(pt, e.yRotO, e.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(pt, e.xRotO, e.getXRot())));

        boolean ready = build >= 1f && ds < 0;
        if (ready) {  // hover and tremble slightly, like it is alive
            pose.translate(0, Mth.sin(t * 0.12f) * 0.02f, 0);
            pose.translate(Mth.sin(t * 37f) * 0.004f, Mth.cos(t * 53f) * 0.004f, 0);
        } else if (build < 1f) {  // while forming it writhes a little
            float w = 0.012f * (1f - build);
            pose.translate(Mth.sin(t * 1.3f) * w, Mth.cos(t * 1.7f) * w, 0);
        }

        // shot: kicks backward, muzzle flips up, and swells for a moment
        int fired = e.firedTick();
        float kick = 0f;
        if (fired >= 0 && t - fired < 10f) {
            kick = 1f - (t - fired) / 10f;
            kick = kick * kick;
            pose.translate(0, 0.03f * kick * k.sy, -0.6f * kick * k.sz);
            pose.mulPose(Axis.XP.rotationDegrees(-9f * kick));
        }
        float swell = 1f + 0.07f * kick;

        // glow: pulses while ready, flares when it finishes forming and when it shoots
        float glow = 0.10f;
        if (build < 1f) glow += 0.18f + 0.12f * Mth.sin(t * 0.9f);
        else if (ready) glow += 0.08f * (1f + Mth.sin(t * 0.2f));
        float sinceReady = t - k.buildTicks;
        if (sinceReady >= 0f && sinceReady < 10f) glow += 0.7f * (1f - sinceReady / 10f);
        glow += 0.9f * kick;

        pose.scale(-k.sx * swell, -k.sy * swell, k.sz * swell); // vanilla model-space flip (Y down) at the ability's size
        model.setup(build);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.renderToBuffer(pose, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, alpha);
        if (glow > 0.03f) {   // additive "eyes" pass slightly larger than the body
            pose.pushPose();
            pose.scale(1.035f, 1.035f, 1.035f);
            VertexConsumer gv = buffer.getBuffer(RenderType.eyes(TEXTURE));
            model.renderToBuffer(pose, gv, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    1f, 0.3f, 0.3f, Math.min(1f, glow) * alpha);
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ShikiGunEntity entity) {
        return TEXTURE;
    }
}
