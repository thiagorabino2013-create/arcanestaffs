package dev.maioral.arcanestaffs.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** A staff that casts a spell on right-click, then goes on cooldown and loses 1 durability. */
public class ElementalStaffItem extends Item {

	@FunctionalInterface
	public interface Spell {
		void cast(ServerWorld world, PlayerEntity caster);
	}

	private final Spell spell;
	private final int cooldownTicks;

	public ElementalStaffItem(Item.Settings settings, int cooldownTicks, Spell spell) {
		super(settings);
		this.cooldownTicks = cooldownTicks;
		this.spell = spell;
	}

	@Override
	public ActionResult use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		if (world instanceof ServerWorld serverWorld) {
			spell.cast(serverWorld, user);
			user.getItemCooldownManager().set(stack, cooldownTicks);
			stack.damage(1, user, LivingEntity.getSlotForHand(hand));
		}
		return ActionResult.SUCCESS;
	}
}
