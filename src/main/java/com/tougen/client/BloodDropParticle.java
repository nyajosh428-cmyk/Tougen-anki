package com.tougen.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/** A falling drop of blood: used for blood rain and for the dripping Shiki gun. */
public class BloodDropParticle extends TextureSheetParticle {

    protected BloodDropParticle(ClientLevel level, double x, double y, double z, double xs, double ys, double zs) {
        super(level, x, y, z);
        this.xd = xs;
        this.yd = ys;
        this.zd = zs;
        this.gravity = 0.9f;
        this.lifetime = 70;
        this.quadSize = 0.07f + this.random.nextFloat() * 0.05f;
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) this.remove();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xs, double ys, double zs) {
            BloodDropParticle p = new BloodDropParticle(level, x, y, z, xs, ys, zs);
            p.pickSprite(sprites);
            return p;
        }
    }
}
