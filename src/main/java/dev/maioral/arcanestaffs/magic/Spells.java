package dev.maioral.arcanestaffs.magic;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;
import java.util.Optional;

/**
 * All the spells live here. Each one is a plain method (ServerWorld, PlayerEntity),
 * so adding a new staff = write a method + register it in ModItems.
 */
public final class Spells {
	private Spells() {}

	// ------------------------------------------------------------------ FIRE
	/** Fire beam. Burns and damages everything near the impact point. */
	public static void fire(ServerWorld world, PlayerEntity caster) {
		Trace t = trace(world, caster, 24);
		sound(world, caster.getEyePos(), SoundEvents.ENTITY_BLAZE_SHOOT, 1.0F);
		beam(world, caster, t.end(), ParticleTypes.FLAME);

		Vec3d p = t.end();
		world.spawnParticles(ParticleTypes.EXPLOSION, p.x, p.y, p.z, 1, 0, 0, 0, 0);
		world.spawnParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 12, 0.6, 0.4, 0.6, 0.1);
		world.spawnParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 15, 0.6, 0.4, 0.6, 0.05);
		sound(world, p, SoundEvents.ITEM_FIRECHARGE_USE, 0.8F);

		for (LivingEntity e : near(world, caster, p, 2.5)) {
			e.damage(world, world.getDamageSources().indirectMagic(caster, caster), 7.0F);
			e.setOnFireFor(6);
		}
	}

	// ------------------------------------------------------------------- ICE
	/** Frost bolt. Damages, heavily slows and freezes enemies at the impact point. */
	public static void ice(ServerWorld world, PlayerEntity caster) {
		Trace t = trace(world, caster, 20);
		sound(world, caster.getEyePos(), SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, 0.8F);
		beam(world, caster, t.end(), ParticleTypes.SNOWFLAKE);

		Vec3d p = t.end();
		world.spawnParticles(ParticleTypes.SNOWFLAKE, p.x, p.y, p.z, 40, 1.0, 1.0, 1.0, 0.05);
		sound(world, p, SoundEvents.BLOCK_GLASS_BREAK, 1.2F);

		for (LivingEntity e : near(world, caster, p, 2.5)) {
			e.damage(world, world.getDamageSources().indirectMagic(caster, caster), 4.0F);
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 3));
			e.setFrozenTicks(e.getMinFreezeDamageTicks() + 60);
		}
	}

	// ------------------------------------------------------------- LIGHTNING
	/** Calls a real lightning bolt where you point (long range, long cooldown). */
	public static void lightning(ServerWorld world, PlayerEntity caster) {
		Trace t = trace(world, caster, 40);
		beam(world, caster, t.end(), ParticleTypes.ELECTRIC_SPARK);

		LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
		if (bolt != null) {
			bolt.setPosition(t.end());
			if (caster instanceof ServerPlayerEntity player) {
				bolt.setChanneler(player);
			}
			world.spawnEntity(bolt);
		}
	}

	// ----------------------------------------------------------------- EARTH
	/** Ground shockwave in front of you: damages, launches and slows everything in the cone. */
	public static void earth(ServerWorld world, PlayerEntity caster) {
		Vec3d look = caster.getRotationVec(1.0F);
		Vec3d flat = new Vec3d(look.x, 0, look.z).normalize();

		for (int i = 1; i <= 8; i++) {
			double x = caster.getX() + flat.x * i;
			double y = caster.getY();
			double z = caster.getZ() + flat.z * i;
			BlockState ground = world.getBlockState(BlockPos.ofFloored(x, y, z).down());
			if (ground.isAir()) continue;
			world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, ground),
					x, y + 0.1, z, 20, 0.4, 0.2, 0.4, 0.15);
		}
		sound(world, caster.getEyePos(), SoundEvents.BLOCK_GRAVEL_BREAK, 0.7F);
		sound(world, caster.getEyePos(), SoundEvents.BLOCK_STONE_BREAK, 0.8F);

		for (LivingEntity e : cone(world, caster, 8, 0.7)) {
			e.damage(world, world.getDamageSources().indirectMagic(caster, caster), 6.0F);
			e.addVelocity(0, 0.9, 0);
			e.velocityModified = true;
			e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
		}
	}

	// ------------------------------------------------------------------ WIND
	/** Blast that shoves enemies away. Sneak + cast to dash through the air instead. */
	public static void wind(ServerWorld world, PlayerEntity caster) {
		Vec3d look = caster.getRotationVec(1.0F);
		sound(world, caster.getEyePos(), SoundEvents.ENTITY_BREEZE_WIND_BURST, 1.0F);

		if (caster.isSneaking()) {
			caster.addVelocity(look.x * 1.8, look.y * 1.2 + 0.3, look.z * 1.8);
			caster.velocityModified = true;
			caster.fallDistance = 0;
			world.spawnParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY() + 0.2, caster.getZ(),
					25, 0.4, 0.2, 0.4, 0.05);
			return;
		}

		Vec3d eye = caster.getEyePos();
		for (int i = 1; i <= 8; i++) {
			Vec3d pos = eye.add(look.multiply(i));
			double spread = 0.2 + i * 0.12;
			world.spawnParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z, 4, spread, spread * 0.6, spread, 0.02);
		}
		for (LivingEntity e : cone(world, caster, 9, 0.5)) {
			Vec3d away = e.getBoundingBox().getCenter().subtract(eye).normalize();
			e.addVelocity(away.x * 2.0, 0.5, away.z * 2.0);
			e.velocityModified = true;
			e.damage(world, world.getDamageSources().indirectMagic(caster, caster), 2.0F);
		}
	}

	// =================================================================== helpers

	private record Trace(Vec3d end, LivingEntity hit) {}

	/** Hitscan: first block or living entity along the caster's line of sight. */
	private static Trace trace(ServerWorld world, PlayerEntity caster, double range) {
		Vec3d start = caster.getEyePos();
		Vec3d dir = caster.getRotationVec(1.0F);
		Vec3d far = start.add(dir.multiply(range));

		BlockHitResult block = world.raycast(new RaycastContext(
				start, far, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
		Vec3d limit = block.getType() == HitResult.Type.MISS ? far : block.getPos();

		LivingEntity hit = null;
		Vec3d hitPos = limit;
		double best = start.squaredDistanceTo(limit);

		Box search = caster.getBoundingBox().stretch(dir.multiply(range)).expand(1.0);
		List<LivingEntity> candidates = world.getEntitiesByClass(LivingEntity.class, search,
				c -> c != caster && c.isAlive() && !c.isSpectator());
		for (LivingEntity cand : candidates) {
			Optional<Vec3d> r = cand.getBoundingBox().expand(0.3).raycast(start, limit);
			if (r.isPresent()) {
				double d = start.squaredDistanceTo(r.get());
				if (d < best) {
					best = d;
					hit = cand;
					hitPos = r.get();
				}
			}
		}
		return new Trace(hitPos, hit);
	}

	/** Living entities (except the caster) within radius of a point. */
	private static List<LivingEntity> near(ServerWorld world, PlayerEntity caster, Vec3d center, double radius) {
		return world.getEntitiesByClass(LivingEntity.class,
				Box.of(center, radius * 2, radius * 2, radius * 2),
				e -> e != caster && e.isAlive() && e.squaredDistanceTo(center) <= radius * radius);
	}

	/** Living entities inside a cone in front of the caster. minDot: 1 = laser, 0 = half-sphere. */
	private static List<LivingEntity> cone(ServerWorld world, PlayerEntity caster, double range, double minDot) {
		Vec3d eye = caster.getEyePos();
		Vec3d look = caster.getRotationVec(1.0F);
		return world.getEntitiesByClass(LivingEntity.class,
				caster.getBoundingBox().expand(range),
				e -> {
					if (e == caster || !e.isAlive()) return false;
					Vec3d to = e.getBoundingBox().getCenter().subtract(eye);
					double dist = to.length();
					return dist <= range && dist > 0.01 && to.normalize().dotProduct(look) >= minDot;
				});
	}

	/** Draws a particle line from just below the caster's eyes to the target. */
	private static void beam(ServerWorld world, PlayerEntity caster, Vec3d end, ParticleEffect particle) {
		Vec3d from = caster.getEyePos().add(0, -0.25, 0);
		Vec3d delta = end.subtract(from);
		double len = delta.length();
		if (len < 1.0) return;
		Vec3d step = delta.normalize().multiply(0.4);
		int count = (int) (len / 0.4);
		for (int i = 2; i < count; i++) {
			Vec3d pos = from.add(step.multiply(i));
			world.spawnParticles(particle, pos.x, pos.y, pos.z, 1, 0.02, 0.02, 0.02, 0.0);
		}
	}

	private static void sound(ServerWorld world, Vec3d pos, SoundEvent sound, float pitch) {
		world.playSound(null, pos.x, pos.y, pos.z, sound, SoundCategory.PLAYERS, 1.0F, pitch);
	}
}
