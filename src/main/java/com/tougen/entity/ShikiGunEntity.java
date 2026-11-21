package com.tougen.entity;

import com.tougen.ModParticles;
import com.tougen.data.OniAbility;
import com.tougen.net.Net;
import com.tougen.net.RecoilPacket;
import net.minecraftforge.network.PacketDistributor;
import com.tougen.world.BloodRain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A gun made of blood. It forms in front of the Oni (parts scale in over the ability's build time), then
 * hovers there until the owner left-clicks to fire it. Right-click (aim) pulls it to the centre.
 * Depending on the ability it is a beam cannon, pistol, sniper, rapid-fire gun or the Professional cannon.
 */
public class ShikiGunEntity extends Entity {
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(ShikiGunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> AIM = SynchedEntityData.defineId(ShikiGunEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DISSOLVE = SynchedEntityData.defineId(ShikiGunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FIRED = SynchedEntityData.defineId(ShikiGunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(ShikiGunEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AMMO = SynchedEntityData.defineId(ShikiGunEntity.class, EntityDataSerializers.INT);

    public static final int DISSOLVE_TICKS = 20;
    private static final int IDLE_TIMEOUT = 20 * 90;

    private static final DustParticleOptions BLOOD_DUST = new DustParticleOptions(new Vector3f(0.65f, 0.0f, 0.02f), 1.6f);
    private static final DustParticleOptions TRACER = new DustParticleOptions(new Vector3f(0.85f, 0.02f, 0.03f), 1.0f);

    private UUID ownerId;
    private float pm = 1f;
    private int cooldown = 0;
    private int idle = 0;
    private int lastSeenFired = -1;

    // Professional cannon demolition state
    private boolean demolishing = false;
    private final List<double[]> spheres = new ArrayList<>();
    private int sphereIdx = 0, cx, cy, cz;
    private boolean cursorInit = false;

    public ShikiGunEntity(EntityType<? extends ShikiGunEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // ------------------------------------------------------------------ setup / accessors

    public void setup(ServerPlayer owner, OniAbility kind, float powerMult) {
        this.ownerId = owner.getUUID();
        this.pm = powerMult;
        entityData.set(KIND, kind.ordinal());
        entityData.set(AMMO, kind.ammo);
        entityData.set(OWNER, owner.getId());
        Vec3 pos = anchor(owner);
        this.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), owner.getXRot());
    }

    public OniAbility kind() {
        int i = entityData.get(KIND);
        OniAbility[] all = OniAbility.values();
        return all[i < 0 || i >= all.length ? 0 : i];
    }

    public UUID getOwnerUuid() {
        return ownerId;
    }

    public int getOwnerEntityId() {
        return entityData.get(OWNER);
    }

    public int getAmmo() {
        return entityData.get(AMMO);
    }

    public boolean isAiming() {
        return entityData.get(AIM);
    }

    public void setAiming(boolean on) {
        entityData.set(AIM, on);
    }

    public int dissolveStart() {
        return entityData.get(DISSOLVE);
    }

    public int firedTick() {
        return entityData.get(FIRED);
    }

    public boolean isDissolving() {
        return entityData.get(DISSOLVE) >= 0;
    }

    public void dismiss() {
        if (!demolishing) startDissolve();
    }

    private void startDissolve() {
        if (!isDissolving()) entityData.set(DISSOLVE, tickCount);
    }

    private Vec3 anchor(ServerPlayer owner) {
        OniAbility k = kind();
        Vec3 look = owner.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : right.normalize();
        boolean aim = isAiming();
        double side = aim ? 0.0 : k.right;
        double down = aim ? k.down * 0.55 : k.down;
        return owner.getEyePosition().add(look.scale(k.fwd)).add(right.scale(side)).add(0, -down, 0);
    }

    private Vec3 muzzle() {
        return position().add(Vec3.directionFromRotation(getXRot(), getYRot()).scale(3.0 * kind().sz));
    }

    // ------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) clientTick();
        else serverTick();
    }

    private void serverTick() {
        ServerLevel sl = (ServerLevel) level();
        ServerPlayer owner = ownerId == null ? null : sl.getServer().getPlayerList().getPlayer(ownerId);
        boolean ownerOk = owner != null && owner.isAlive() && owner.level() == sl;
        if (!ownerOk && !demolishing) {
            discard();
            return;
        }
        OniAbility k = kind();
        if (ownerOk) {
            Vec3 pos = anchor(owner);
            this.moveTo(pos.x, pos.y, pos.z, owner.getYRot(), owner.getXRot());
        }
        if (cooldown > 0) cooldown--;

        boolean forming = tickCount < k.buildTicks;
        if (forming && ownerOk) {
            formEffects(sl, owner, k);
            if (tickCount % 14 == 0) {
                sl.playSound(null, getX(), getY(), getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.2f,
                        0.7f + tickCount / 200f);
            }
        } else if (tickCount == k.buildTicks) {
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.5f, 0.6f);
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5f, 0.5f);
            Vec3 f = Vec3.directionFromRotation(getXRot(), getYRot());
            ring(sl, position(), f, 1.0 * k.sz, 0.45, 32);
            ring(sl, position(), f, 0.5 * k.sz, 0.25, 20);
        }

        if (demolishing) {
            demolishStep(sl);
        } else if (!forming && !isDissolving()) {
            if (++idle > IDLE_TIMEOUT) startDissolve();
        }

        if (isDissolving() && !demolishing && tickCount - dissolveStart() >= DISSOLVE_TICKS) {
            discard();
        }
    }

    private static Vec3[] basis(Vec3 f) {
        Vec3 right = f.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : right.normalize();
        Vec3 up = right.cross(f).normalize();
        return new Vec3[]{right, up};
    }

    private Vec3 handPos(ServerPlayer owner) {
        Vec3 look = owner.getLookAngle();
        Vec3 right = basis(look)[0];
        return owner.getEyePosition().add(look.scale(0.5)).add(right.scale(0.35)).add(0, -0.45, 0);
    }

    /** World position of a gun part's centre (see GunParts). */
    private Vec3 partWorld(int i) {
        OniAbility k = kind();
        Vec3 f = Vec3.directionFromRotation(getXRot(), getYRot());
        Vec3[] b = basis(f);
        float[] p = GunParts.POS[i];
        return position().add(b[0].scale(p[0] / 16.0 * k.sx)).add(b[1].scale(p[1] / 16.0 * k.sy))
                .add(f.scale(p[2] / 16.0 * k.sz));
    }

    /**
     * Forming animation (server half): a pool of blood at the hand, and curved tendrils that arc from the
     * hand to whichever part is currently congealing. When a part finishes it splashes and squelches.
     */
    private void formEffects(ServerLevel sl, ServerPlayer owner, OniAbility k) {
        float f = (float) tickCount / Math.max(1, k.buildTicks);
        Vec3 hand = handPos(owner);
        sl.sendParticles(BLOOD_DUST, hand.x, hand.y, hand.z, 3, 0.1, 0.1, 0.1, 0.01);
        for (int i = 0; i < GunParts.COUNT; i++) {
            if (f > GunParts.START[i] && f <= GunParts.END[i]) {
                Vec3 target = partWorld(i);
                Vec3 mid = hand.add(target).scale(0.5).add((random.nextDouble() - 0.5) * 0.8,
                        0.6 + random.nextDouble() * 0.5, (random.nextDouble() - 0.5) * 0.8);
                for (int j = 0; j < 3; j++) {
                    double t = random.nextDouble(), u = 1 - t;
                    Vec3 pt = hand.scale(u * u).add(mid.scale(2 * u * t)).add(target.scale(t * t));
                    sl.sendParticles(BLOOD_DUST, pt.x, pt.y, pt.z, 1, 0.02, 0.02, 0.02, 0.0);
                }
            }
            if (tickCount == Math.round(GunParts.END[i] * k.buildTicks)) partDone(sl, i);
        }
    }

    private void partDone(ServerLevel sl, int i) {
        Vec3 p = partWorld(i);
        for (int n = 0; n < 8; n++) {
            sl.sendParticles(ModParticles.BLOOD_DROP.get(), p.x, p.y, p.z, 0,
                    (random.nextDouble() - 0.5) * 0.5, 0.1 + random.nextDouble() * 0.3, (random.nextDouble() - 0.5) * 0.5, 1.0);
        }
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE, p.x, p.y, p.z, 6, 0.2, 0.2, 0.2, 0.02);
        sl.playSound(null, p.x, p.y, p.z, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.9f, 0.6f + 0.08f * i);
    }

    /** Ring of blood drops flying outward in the plane facing f. */
    private void ring(ServerLevel sl, Vec3 c, Vec3 f, double radius, double speed, int n) {
        Vec3[] b = basis(f);
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            Vec3 d = b[0].scale(Math.cos(a)).add(b[1].scale(Math.sin(a)));
            sl.sendParticles(ModParticles.BLOOD_DROP.get(), c.x + d.x * radius, c.y + d.y * radius, c.z + d.z * radius,
                    0, d.x, d.y + 0.1, d.z, speed);
        }
    }

    private void splash(ServerLevel sl, Vec3 p, int n) {
        for (int i = 0; i < n; i++) {
            sl.sendParticles(ModParticles.BLOOD_DROP.get(), p.x, p.y, p.z, 0,
                    (random.nextDouble() - 0.5) * 0.9, 0.2 + random.nextDouble() * 0.6, (random.nextDouble() - 0.5) * 0.9, 1.0);
        }
    }

    private void recoil(ServerPlayer owner, float pitch) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> owner),
                new RecoilPacket(pitch, (random.nextFloat() - 0.5f) * pitch * 0.4f));
    }

    /** Muzzle blast: flash, shock rings and a cone of blood spray; plus camera kick for the shooter. */
    private void fireFx(ServerLevel sl, ServerPlayer owner, OniAbility k) {
        Vec3 m = muzzle();
        Vec3 f = Vec3.directionFromRotation(getXRot(), getYRot());
        switch (k) {
            case BIG_GUN, MEGA_GUN -> {
                sl.sendParticles(ParticleTypes.FLASH, m.x, m.y, m.z, 1, 0, 0, 0, 0);
                ring(sl, m, f, 0.4 * k.sz, 0.35, 28);
                ring(sl, m, f, 0.2 * k.sz, 0.6, 16);
                for (int i = 0; i < 30; i++) {
                    Vec3 v = f.scale(0.5 + random.nextDouble() * 0.9).add((random.nextDouble() - 0.5) * 0.4,
                            (random.nextDouble() - 0.5) * 0.4, (random.nextDouble() - 0.5) * 0.4);
                    sl.sendParticles(ModParticles.BLOOD_DROP.get(), m.x, m.y, m.z, 0, v.x, v.y, v.z, 1.0);
                }
                recoil(owner, k == OniAbility.MEGA_GUN ? 14f : 7f);
            }
            case SNIPER -> {
                ring(sl, m, f, 0.25, 0.4, 14);
                recoil(owner, 6f);
            }
            case SMALL_GUN -> {
                sl.sendParticles(ParticleTypes.SMOKE, m.x, m.y, m.z, 2, 0.03, 0.03, 0.03, 0.01);
                splash(sl, m, 2);
                recoil(owner, 1.2f);
            }
            default -> {
                sl.sendParticles(ParticleTypes.SMOKE, m.x, m.y, m.z, 1, 0.03, 0.03, 0.03, 0.01);
                recoil(owner, 0.6f);
            }
        }
    }

    // ------------------------------------------------------------------ firing

    /** Called when the owner left-clicks. */
    public void fire(ServerPlayer owner) {
        OniAbility k = kind();
        int ammo = entityData.get(AMMO);
        if (tickCount < k.buildTicks || isDissolving() || cooldown > 0 || demolishing || ammo <= 0) return;

        ServerLevel sl = (ServerLevel) level();
        entityData.set(AMMO, --ammo);
        entityData.set(FIRED, tickCount);
        cooldown = k.delay;
        idle = 0;

        fireFx(sl, owner, k);
        switch (k) {
            case BIG_GUN -> beam(sl, owner, k.damage * pm, 2.3, k.range, 3.0f, 3.0f * pm, 0.7f);
            case MEGA_GUN -> mega(sl, owner);
            case SNIPER -> bullet(sl, owner, k.damage * pm, 0f, k.range, true);
            default -> bullet(sl, owner, k.damage * pm, k.spread, k.range, false);
        }
        if (ammo <= 0 && k != OniAbility.MEGA_GUN) startDissolve();
    }

    private record Ray(Vec3 eye, Vec3 dir, Vec3 stop, boolean missed, double dist) {
    }

    private Ray cast(ServerLevel sl, ServerPlayer owner, Vec3 dir, double range) {
        Vec3 eye = owner.getEyePosition();
        Vec3 end = eye.add(dir.scale(range));
        BlockHitResult bh = sl.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        boolean missed = bh.getType() == HitResult.Type.MISS;
        Vec3 stop = missed ? end : bh.getLocation();
        return new Ray(eye, dir, stop, missed, eye.distanceTo(stop));
    }

    private void maybeRain(ServerLevel sl, ServerPlayer owner, Ray r) {
        if (owner.getXRot() <= -55f && r.missed()) {
            BloodRain.start(sl);
            owner.displayClientMessage(net.minecraft.network.chat.Component.literal("The sky bleeds..."), true);
        }
    }

    private void hurt(ServerPlayer owner, Entity target, float dmg) {
        ShikiAbility.markHit(owner);
        target.invulnerableTime = 0;
        target.hurt(owner.damageSources().playerAttack(owner), dmg);
    }

    /** Wide beam: damages everything along the line. */
    private void beam(ServerLevel sl, ServerPlayer owner, float dmg, double radius, double range,
                      float dustSize, float explosion, float soundPitch) {
        Ray r = cast(sl, owner, owner.getLookAngle(), range);
        AABB box = new AABB(r.eye(), r.stop()).inflate(radius + 0.5);
        for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != owner && e.isAlive())) {
            Vec3 toE = le.getBoundingBox().getCenter().subtract(r.eye());
            double proj = toE.dot(r.dir());
            if (proj < 0 || proj > r.dist() + 1.0) continue;
            if (toE.subtract(r.dir().scale(proj)).length() <= radius) {
                hurt(owner, le, dmg);
                le.push(r.dir().x * 1.6, 0.5, r.dir().z * 1.6);
                le.hurtMarked = true;
            }
        }
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.8f, 0.02f, 0.03f), dustSize);
        sl.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 4.0f, soundPitch);
        sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        Vec3 start = muzzle();
        double len = start.distanceTo(r.stop());
        for (double d = 0; d < len; d += 0.7) {
            Vec3 p = start.add(r.dir().scale(d));
            sl.sendParticles(dust, p.x, p.y, p.z, 3, radius * 0.15, radius * 0.15, radius * 0.15, 0.0);
        }
        Vec3[] bs = basis(r.dir());
        double rad = Math.min(1.3, radius * 0.5);
        DustParticleOptions hot = new DustParticleOptions(new Vector3f(1.0f, 0.15f, 0.15f), 1.2f);
        for (double d = 0; d < len; d += 0.6) {                     // twin spiral wrapped around the beam
            for (int strand = 0; strand < 2; strand++) {
                double a = d * 1.7 + strand * Math.PI;
                Vec3 off = bs[0].scale(Math.cos(a) * rad).add(bs[1].scale(Math.sin(a) * rad));
                Vec3 p = start.add(r.dir().scale(d)).add(off);
                sl.sendParticles(hot, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
        if (!r.missed()) splash(sl, r.stop(), 40);
        if (!r.missed() && r.dist() > 8.0) {
            sl.explode(null, r.stop().x, r.stop().y, r.stop().z, explosion, Level.ExplosionInteraction.NONE);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, r.stop().x, r.stop().y, r.stop().z, 1, 0, 0, 0, 0);
        }
        maybeRain(sl, owner, r);
        owner.setDeltaMovement(owner.getDeltaMovement().add(r.dir().scale(-0.5)));
        owner.hurtMarked = true;
    }

    /** Single hitscan bullet (pistol, sniper, rapid-fire). */
    private void bullet(ServerLevel sl, ServerPlayer owner, float dmg, float spread, double range, boolean sniper) {
        RandomSource rnd = sl.random;
        float yaw = owner.getYRot() + (rnd.nextFloat() - 0.5f) * 2f * spread * (isAiming() ? 0.4f : 1f);
        float pitch = owner.getXRot() + (rnd.nextFloat() - 0.5f) * 2f * spread * (isAiming() ? 0.4f : 1f);
        Vec3 dir = Vec3.directionFromRotation(pitch, yaw);
        Ray r = cast(sl, owner, dir, range);

        AABB box = owner.getBoundingBox().expandTowards(r.stop().subtract(r.eye())).inflate(1.0);
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(sl, owner, r.eye(), r.stop(), box,
                e -> !e.isSpectator() && e.isPickable() && e != owner && e != this);
        Vec3 stop = r.stop();
        if (eh != null) {
            stop = eh.getLocation();
            float d = dmg;
            if (eh.getEntity() instanceof LivingEntity le && stop.y > le.getEyeY() - 0.22) d *= 1.75f;
            hurt(owner, eh.getEntity(), d);
        }
        Vec3 start = muzzle();
        double len = start.distanceTo(stop);
        double step = sniper ? 0.5 : 1.2;
        DustParticleOptions dust = sniper ? new DustParticleOptions(new Vector3f(0.9f, 0.05f, 0.05f), 1.3f) : TRACER;
        for (double d = 0; d < len; d += step) {
            Vec3 p = start.add(dir.scale(d));
            sl.sendParticles(dust, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE, stop.x, stop.y, stop.z, 5, 0.1, 0.1, 0.1, 0.02);
        if (eh != null || !r.missed()) splash(sl, stop, sniper ? 14 : 5);
        if (sniper) {
            sl.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2f, 1.8f);
            maybeRain(sl, owner, r);
        } else {
            sl.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,
                    SoundSource.PLAYERS, spread > 2f ? 0.6f : 1.0f, spread > 2f ? 2.0f : 1.7f);
        }
    }

    // ------------------------------------------------------------------ Professional cannon

    private void mega(ServerLevel sl, ServerPlayer owner) {
        OniAbility k = OniAbility.MEGA_GUN;
        beam(sl, owner, k.damage * pm, 5.0, k.range, 5.0f, 6.0f, 0.5f);

        Ray r = cast(sl, owner, owner.getLookAngle(), k.range);
        spheres.clear();
        sphereIdx = 0;
        cursorInit = false;
        double scale = 0.85 + 0.15 * Math.min(1.0, (pm - 1.0) / 0.8);
        if (!r.missed()) {
            // crater big enough to flatten a woodland mansion
            spheres.add(new double[]{r.stop().x, r.stop().y, r.stop().z, 32 * scale, 24 * scale});
        }
        for (double d = 4; d < r.dist(); d += 3.0) {                 // tunnel along the beam
            Vec3 p = r.eye().add(r.dir().scale(d));
            spheres.add(new double[]{p.x, p.y - 0.3, p.z, 4.5, 4.5});
        }
        // everything alive near the impact takes a huge hit
        if (!r.missed()) {
            AABB area = new AABB(r.stop(), r.stop()).inflate(28);
            for (LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, area, e -> e != owner && e.isAlive())) {
                if (le.position().distanceTo(r.stop()) < 28) hurt(owner, le, 60f * pm);
            }
            sl.playSound(null, r.stop().x, r.stop().y, r.stop().z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 8.0f, 0.5f);
        }
        demolishing = true;
    }

    /** Removes blocks gradually (a few thousand per tick) so the server doesn't stall. Bedrock is untouched. */
    private void demolishStep(ServerLevel sl) {
        int budget = 5000;
        while (budget > 0 && sphereIdx < spheres.size()) {
            double[] s = spheres.get(sphereIdx);
            int minX = (int) Math.floor(s[0] - s[3]), maxX = (int) Math.ceil(s[0] + s[3]);
            int minY = (int) Math.floor(s[1] - s[4]), maxY = (int) Math.ceil(s[1] + s[4]);
            int minZ = (int) Math.floor(s[2] - s[3]);
            int maxZ = (int) Math.ceil(s[2] + s[3]);
            if (!cursorInit) {
                cx = minX;
                cy = minY;
                cz = minZ;
                cursorInit = true;
            }
            while (budget > 0) {
                double dx = (cx + 0.5 - s[0]) / s[3], dy = (cy + 0.5 - s[1]) / s[4], dz = (cz + 0.5 - s[2]) / s[3];
                if (dx * dx + dy * dy + dz * dz <= 1.0) {
                    BlockPos pos = new BlockPos(cx, cy, cz);
                    if (sl.hasChunkAt(pos)) {
                        BlockState st = sl.getBlockState(pos);
                        if (!st.isAir() && st.getDestroySpeed(sl, pos) >= 0f) {
                            sl.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
                            if ((cx + cy + cz) % 211 == 0) {
                                sl.sendParticles(ParticleTypes.EXPLOSION, cx + 0.5, cy + 0.5, cz + 0.5, 1, 0, 0, 0, 0);
                            }
                        }
                    }
                }
                budget--;
                if (++cz > maxZ) {
                    cz = minZ;
                    if (++cy > maxY) {
                        cy = minY;
                        if (++cx > maxX) {
                            sphereIdx++;
                            cursorInit = false;
                            break;
                        }
                    }
                }
            }
        }
        if (sphereIdx >= spheres.size()) {
            demolishing = false;
            startDissolve();
        }
    }

    // ------------------------------------------------------------------ client

    private void clientTick() {
        OniAbility k = kind();
        Vec3 fwd = Vec3.directionFromRotation(getXRot(), getYRot());
        if (tickCount < k.buildTicks) {
            for (int i = 0; i < 2; i++) {
                double along = (random.nextDouble() - 0.5) * 3.0 * k.sz;
                Vec3 pt = position().add(fwd.scale(along));
                level().addParticle(ModParticles.BLOOD_DROP.get(), pt.x + (random.nextDouble() - 0.5) * 0.4 * k.sx,
                        pt.y + random.nextDouble() * 0.4 * k.sy, pt.z + (random.nextDouble() - 0.5) * 0.4 * k.sx, 0, 0, 0);
            }
        }
        int fired = firedTick();
        if (fired != lastSeenFired) {
            if (lastSeenFired != -1 || fired >= 0) {
                Vec3 muzzle = position().add(fwd.scale(3.0 * k.sz));
                int n = k == OniAbility.BIG_GUN || k == OniAbility.MEGA_GUN ? 40 : 6;
                for (int i = 0; i < n; i++) {
                    level().addParticle(ModParticles.BLOOD_DROP.get(), muzzle.x, muzzle.y, muzzle.z,
                            (random.nextDouble() - 0.5) * 0.5, random.nextDouble() * 0.3, (random.nextDouble() - 0.5) * 0.5);
                }
            }
            lastSeenFired = fired;
        }
    }

    // ------------------------------------------------------------------ boilerplate

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(14.0);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(KIND, 0);
        entityData.define(AIM, false);
        entityData.define(DISSOLVE, -1);
        entityData.define(FIRED, -1);
        entityData.define(OWNER, -1);
        entityData.define(AMMO, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        super.remove(reason);
        if (!level().isClientSide) ShikiAbility.onGunRemoved(this);
    }
}
