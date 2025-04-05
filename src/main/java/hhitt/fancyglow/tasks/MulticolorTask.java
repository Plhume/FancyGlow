package hhitt.fancyglow.tasks;

import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.UUID;

public class MulticolorTask extends BukkitRunnable {

    private int currentIndex = 0;
    private final GlowManager glowManager;
    private final PlayerGlowManager playerGlowManager;

    public MulticolorTask(FancyGlow plugin) {
        this.glowManager = plugin.getGlowManager();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    @Override
    public void run() {
        if (glowManager.getMulticolorPlayerSet().isEmpty()) return;

        Team currentTeam = glowManager.getOrCreateTeam(GlowManager.COLORS_ARRAY[currentIndex]);

        Player player;
        Team lastTeam;
        for (UUID uuid : glowManager.getMulticolorPlayerSet()) {
            player = Objects.requireNonNull(Bukkit.getPlayer(uuid));
            if (player.isDead()) continue;

            lastTeam = playerGlowManager.findPlayerTeam(player);

            currentTeam.addEntry(player.getName());

            if (lastTeam != null) {
                lastTeam.removeEntry(player.getName());
            }

            if (!player.isGlowing()) {
                player.setGlowing(true);
            }
            if (currentTeam.getScoreboard() != null && player.getScoreboard() != currentTeam.getScoreboard()) {
                player.setScoreboard(currentTeam.getScoreboard());
            }
        }

        currentIndex = (currentIndex + 1) % GlowManager.COLORS_ARRAY.length;
    }
}