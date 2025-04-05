package hhitt.fancyglow.listeners;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final FancyGlow plugin;
    private final GlowManager glowManager;

    public PlayerQuitListener(FancyGlow plugin) {
        this.plugin = plugin;
        this.glowManager = plugin.getGlowManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (glowManager.isFlashingTaskActive(player)) {
            glowManager.disableFlashing(player);
        }

        if (glowManager.isMulticolorTaskActive(player)) {
            glowManager.disableRainbow(player);
        }

        //TODO: Improve this thing?
        if (!plugin.getConfiguration().getBoolean("Persistent_Mode") && player.isGlowing()) {
            glowManager.removeGlow(player);
        }
    }
}
