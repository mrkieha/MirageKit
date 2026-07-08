package dev.mrkieha.mirage;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric entrypoint for Mirage.
 *
 * @since 1.0
 */
public class MirageMod implements ModInitializer {

    public static final String MOD_ID = "mirage";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Mirage] Server-side display toolkit ready.");
        Mirage.hookTick();
    }
}
