package com.tougen.gun;

import com.mojang.math.Axis;
import com.tougen.ModItems;
import com.tougen.net.Net;
import com.tougen.net.RecoilPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class GunItem extends Item {
    public static final String AMMO = "Ammo";
    public static final String RELOAD_END = "ReloadEnd";

    public final GunSpec spec;

    public GunItem(GunSpec spec) {
        super(new Item.Properties().stacksTo(1));
        this.spec = spec;
    }

    // ---------------------------------------------------------------- state helpers

    public static int getAmmo(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(AMMO) : 0;
    }

    public static long getReloadEnd(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(RELOAD_END) ? stack.getTag().getLong(RELOAD_END) : -1L;
    }

    public static boolean isReloading(ItemStack stack) {
        return getReloadEnd(stack) >= 0L;
    }

    public static int countBullets(Player p) {
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(ModItems.BULLET.get())) n += s.getCount();
        }
        return n;
    }

    private static int takeBullets(Player p, int want) {
        int taken = 0;
        for (int i = 0; i < p.getInventory().getContainerSize() && taken < want; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(ModItems.BULLET.get())) {
                int n = Math.min(s.getCount(), want - taken);
                s.shrink(n);
                taken += n;
            }
        }
        return taken;
    }

    // ---------------------------------------------------------------- using

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isReloading(stack)) return InteractionResultHolder.fail(stack);

        if (getAmmo(stack) <= 0) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.DISPENSER_FAIL,
                        SoundSource.PLAYERS, 0.6f, 1.8f);
                startReload(sp, stack);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (spec.auto) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        if (!player.getCooldowns().isOnCooldown(this)) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) fire(sp, stack);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide || !(entity instanceof ServerPlayer sp)) return;
        if (isReloading(stack) || getAmmo(stack) <= 0) {
            sp.stopUsingItem();
            return;
        }
        if (!sp.getCooldowns().isOnCooldown(this)) fire(sp, stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

    // ---------------------------------------------------------------- shooting

    private void fire(ServerPlayer p, ItemStack stack) {
        ServerLevel level = p.serverLevel();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(AMMO, Math.max(0, tag.getInt(AMMO) - 1));
        p.getCooldowns().addCooldown(this, spec.delay);

        Vec3 eye = p.getEyePosition();
        float spread = spec.spread * (p.isShiftKeyDown() ? 0.5f : 1.0f);
        for (int i = 0; i < spec.pellets; i++) {
            shoot(p, level, eye, spreadDir(p, level.random, spread));
        }

        Vec3 look = p.getLookAngle();
        Vec3 muzzle = eye.add(look.scale(1.3)).add(0, -0.12, 0);
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.04, 0.04, 0.04, 0.01);
        level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 1, 0.02, 0.02, 0.02, 0.005);

        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 2.0f, spec.soundPitch);
        if (spec.damage >= 15f) {
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS, 0.5f, 1.6f);
        }

        float rec = spec.recoil * (p.isShiftKeyDown() ? 0.6f : 1.0f);
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),
                new RecoilPacket(rec, (level.random.nextFloat() - 0.5f) * rec * 0.5f));

        if (tag.getInt(AMMO) <= 0) {
            p.displayClientMessage(Component.literal("Empty - press R to reload"), true);
        }
    }

    private static Vec3 spreadDir(Player p, RandomSource rnd, float spread) {
        float yaw = p.getYRot() + (rnd.nextFloat() - 0.5f) * 2f * spread;
        float pitch = p.getXRot() + (rnd.nextFloat() - 0.5f) * 2f * spread;
        return Vec3.directionFromRotation(pitch, yaw);
    }

    private void shoot(ServerPlayer p, ServerLevel level, Vec3 eye, Vec3 dir) {
        Vec3 end = eye.add(dir.scale(spec.range));
        BlockHitResult bh = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, p));
        Vec3 stop = bh.getType() == HitResult.Type.MISS ? end : bh.getLocation();

        AABB box = p.getBoundingBox().expandTowards(stop.subtract(eye)).inflate(1.0);
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(level, p, eye, stop, box,
                e -> !e.isSpectator() && e.isPickable() && e != p);

        if (eh != null) {
            stop = eh.getLocation();
            Entity target = eh.getEntity();
            float dmg = spec.damage;
            if (target instanceof LivingEntity le && stop.y > le.getEyeY() - 0.22) dmg *= 1.75f;
            target.invulnerableTime = 0;
            target.hurt(p.damageSources().playerAttack(p), dmg);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
                    net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    stop.x, stop.y, stop.z, 6, 0.1, 0.1, 0.1, 0.05);
        } else if (bh.getType() == HitResult.Type.BLOCK) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
                    level.getBlockState(bh.getBlockPos())),
                    stop.x, stop.y, stop.z, 6, 0.08, 0.08, 0.08, 0.05);
        }

        // sparse tracer
        double dist = eye.distanceTo(stop);
        for (double d = 2.0; d < dist; d += 3.0) {
            Vec3 pt = eye.add(dir.scale(d));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y - 0.1, pt.z, 1, 0, 0, 0, 0);
        }
    }

    // ---------------------------------------------------------------- reloading

    public void startReload(ServerPlayer p, ItemStack stack) {
        if (isReloading(stack) || getAmmo(stack) >= spec.magSize) return;
        if (countBullets(p) <= 0) {
            p.displayClientMessage(Component.literal("No bullets! Craft them from iron."), true);
            return;
        }
        ServerLevel level = p.serverLevel();
        stack.getOrCreateTag().putLong(RELOAD_END, level.getGameTime() + spec.reloadTicks);
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CROSSBOW_LOADING_START,
                SoundSource.PLAYERS, 0.9f, 1.3f);
        p.stopUsingItem();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof ServerPlayer p)) return;
        long end = getReloadEnd(stack);
        if (end < 0L) return;

        if (!selected) { // switching away cancels the reload
            stack.getTag().remove(RELOAD_END);
            return;
        }
        long now = level.getGameTime();
        if (now == end - spec.reloadTicks / 2) {
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.IRON_TRAPDOOR_OPEN,
                    SoundSource.PLAYERS, 0.8f, 1.8f);
        }
        if (now >= end) {
            CompoundTag tag = stack.getTag();
            int need = spec.magSize - tag.getInt(AMMO);
            int got = takeBullets(p, need);
            tag.putInt(AMMO, tag.getInt(AMMO) + got);
            tag.remove(RELOAD_END);
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.PLAYERS, 0.9f, 1.5f);
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.CROSSBOW_LOADING_END,
                    SoundSource.PLAYERS, 0.9f, 1.4f);
        }
    }

    // ---------------------------------------------------------------- tooltip

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal(spec.category + " - " + spec.blurb)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        tip.add(Component.literal("Ammo: " + getAmmo(stack) + " / " + spec.magSize)
                .withStyle(net.minecraft.ChatFormatting.GOLD));
        String dmg = spec.pellets > 1 ? spec.damage + " x" + spec.pellets : String.valueOf(spec.damage);
        tip.add(Component.literal("Damage: " + dmg + "  |  " + (spec.auto ? "Full-auto (hold)" : "Semi-auto"))
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        tip.add(Component.literal("R = reload (uses universal bullets)")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }

    // ---------------------------------------------------------------- client reload animation

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick, float equipProcess,
                                                   float swingProcess) {
                long end = getReloadEnd(stack);
                if (end < 0L) return false;

                float now = player.level().getGameTime() + partialTick;
                float t = Mth.clamp(1.0f - (end - now) / (float) spec.reloadTicks, 0f, 1f);
                int side = arm == HumanoidArm.RIGHT ? 1 : -1;

                pose.translate(side * 0.56f, -0.52f + equipProcess * -0.6f, -0.72f);

                float dip = Mth.sin(t * Mth.PI);                 // 0 -> 1 -> 0
                float ease = Mth.clamp(dip * 2.5f, 0f, 1f);
                pose.translate(0f, -0.32f * ease, 0.06f * ease);
                pose.mulPose(Axis.XP.rotationDegrees(32f * ease));   // muzzle up
                pose.mulPose(Axis.ZP.rotationDegrees(side * 24f * ease)); // roll to show the magazine

                // magazine "swap" bump in the middle of the reload
                float bump = (t > 0.40f && t < 0.62f) ? Mth.sin((t - 0.40f) / 0.22f * Mth.PI) : 0f;
                pose.translate(0f, -0.06f * bump, 0.04f * bump);
                pose.mulPose(Axis.XP.rotationDegrees(-9f * bump));

                // final "slide rack" snap
                float rack = (t > 0.82f && t < 0.95f) ? Mth.sin((t - 0.82f) / 0.13f * Mth.PI) : 0f;
                pose.translate(0f, 0f, 0.05f * rack);
                return true;
            }
        });
    }
}
