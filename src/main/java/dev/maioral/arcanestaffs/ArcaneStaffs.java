package dev.maioral.arcanestaffs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcaneStaffs implements ModInitializer {
	public static final String MOD_ID = "arcanestaffs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		LOGGER.info("Arcane Staffs loaded. Go cast something.");
	}
}
