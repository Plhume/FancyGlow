package hhitt.fancyglow.tasks;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

public class FlashingTask extends BukkitRunnable {

    private final GlowManager glowManager;

    public FlashingTask(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
    }

    @Override
    public void run() {
        if (glowManager.getFlashingPlayerSet().isEmpty()) return;

        Player player;
        for (UUID uuid : glowManager.getFlashingPlayerSet()) {
            player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
            if (player.isDead()) continue;

            player.setGlowing(!player.isGlowing());
        }
    }
}