package com.tougen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import com.tougen.entity.GunParts;

/**
 * Shiki's Big Gun, assembled from blood. Every part has its own scale-in window so the
 * gun visibly constructs itself: core, then barrel, stock, grip, magazine, sight, rails, horns.
 * Authored in vanilla model space (Y points down), the renderer applies the usual (-1,-1,1) flip.
 */
public class ShikiGunModel extends Model {
    private final ModelPart root;
    private final ModelPart receiver, barrel, muzzle, stock, grip, magazine, scope, railL, railR, spikeL, spikeR;

    public ShikiGunModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        receiver = root.getChild("receiver");
        barrel = root.getChild("barrel");
        muzzle = root.getChild("muzzle");
        stock = root.getChild("stock");
        grip = root.getChild("grip");
        magazine = root.getChild("magazine");
        scope = root.getChild("scope");
        railL = root.getChild("rail_l");
        railR = root.getChild("rail_r");
        spikeL = root.getChild("spike_l");
        spikeR = root.getChild("spike_r");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        CubeListBuilder c = CubeListBuilder.create();

        r.addOrReplaceChild("receiver", CubeListBuilder.create().texOffs(0, 0).addBox(-6, -6, -14, 12, 12, 28), PartPose.ZERO);
        r.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -3, 0, 6, 6, 30), PartPose.offset(0, 0, 14));
        r.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -5, -3, 10, 10, 6), PartPose.offset(0, 0, 46));
        r.addOrReplaceChild("stock", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -7, -22, 8, 12, 22), PartPose.offset(0, 1, -14));
        r.addOrReplaceChild("grip", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 0, -3, 6, 14, 7), PartPose.offset(0, 6, -6));
        r.addOrReplaceChild("magazine", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 0, -5, 6, 18, 10), PartPose.offset(0, 6, 4));
        r.addOrReplaceChild("scope", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -5, -10, 6, 5, 20), PartPose.offset(0, -6, -2));
        r.addOrReplaceChild("rail_l", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -2, 0, 2, 4, 18), PartPose.offset(-7, 0, 2));
        r.addOrReplaceChild("rail_r", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -2, 0, 2, 4, 18), PartPose.offset(7, 0, 2));
        r.addOrReplaceChild("spike_l", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -8, -1, 2, 8, 2), PartPose.offset(-4, -6, 10));
        r.addOrReplaceChild("spike_r", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -8, -1, 2, 8, 2), PartPose.offset(4, -6, 10));
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void scale(ModelPart part, float sx, float sy, float sz) {
        part.xScale = sx;
        part.yScale = sy;
        part.zScale = sz;
        part.visible = sx * sy * sz > 0.0001f;
    }

    /** @param build 0..1 construction progress */
    public void setup(float build) {
        float p = GunParts.stage(build, 0);
        scale(receiver, p, p, p);

        p = GunParts.stage(build, 1);
        scale(barrel, 0.4f + 0.6f * p, 0.4f + 0.6f * p, p);

        p = GunParts.stage(build, 2);
        scale(muzzle, p, p, p);

        p = GunParts.stage(build, 3);
        scale(stock, 0.5f + 0.5f * p, 0.5f + 0.5f * p, p);

        p = GunParts.stage(build, 4);
        scale(grip, 0.6f + 0.4f * p, p, 0.6f + 0.4f * p);

        p = GunParts.stage(build, 5);
        scale(magazine, 0.6f + 0.4f * p, p, 0.6f + 0.4f * p);

        p = GunParts.stage(build, 6);
        scale(scope, 0.6f + 0.4f * p, p, 0.6f + 0.4f * p);

        p = GunParts.stage(build, 7);
        scale(railL, 1f, 1f, p);
        scale(railR, 1f, 1f, p);

        p = GunParts.stage(build, 8);
        scale(spikeL, 1f, p, 1f);
        scale(spikeR, 1f, p, 1f);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay,
                               float r, float g, float b, float a) {
        root.render(pose, vc, light, overlay, r, g, b, a);
    }
}
