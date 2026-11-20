package dev.maioral.arcanestaffs;

import dev.maioral.arcanestaffs.item.ElementalStaffItem;
import dev.maioral.arcanestaffs.magic.Spells;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModItems {
	private ModItems() {}

	public static Item FIRE_STAFF;
	public static Item ICE_STAFF;
	public static Item LIGHTNING_STAFF;
	public static Item EARTH_STAFF;
	public static Item WIND_STAFF;

	/** Registers all staffs and the creative tab. Called from ArcaneStaffs#onInitialize. */
	public static void register() {
		//                       name              cooldown(ticks) durability  spell
		FIRE_STAFF      = staff("fire_staff",      25, 250, Spells::fire);
		ICE_STAFF       = staff("ice_staff",       30, 250, Spells::ice);
		LIGHTNING_STAFF = staff("lightning_staff", 80, 120, Spells::lightning);
		EARTH_STAFF     = staff("earth_staff",     50, 200, Spells::earth);
		WIND_STAFF      = staff("wind_staff",      40, 300, Spells::wind);

		RegistryKey<ItemGroup> groupKey =
				RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(ArcaneStaffs.MOD_ID, "main"));
		Registry.register(Registries.ITEM_GROUP, groupKey, FabricItemGroup.builder()
				.icon(() -> new ItemStack(FIRE_STAFF))
				.displayName(Text.translatable("itemGroup." + ArcaneStaffs.MOD_ID))
				.entries((context, entries) -> {
					entries.add(FIRE_STAFF);
					entries.add(ICE_STAFF);
					entries.add(LIGHTNING_STAFF);
					entries.add(EARTH_STAFF);
					entries.add(WIND_STAFF);
				})
				.build());
	}

	private static Item staff(String name, int cooldownTicks, int durability, ElementalStaffItem.Spell spell) {
		Identifier id = Identifier.of(ArcaneStaffs.MOD_ID, name);
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item.Settings settings = new Item.Settings()
				.registryKey(key)
				.maxCount(1)
				.maxDamage(durability)
				.rarity(Rarity.RARE)
				.component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
		Item item = Registry.register(Registries.ITEM, key, new ElementalStaffItem(settings, cooldownTicks, spell));
		ArcaneStaffs.LOGGER.info("Registered item {}", id);
		return item;
	}
}
