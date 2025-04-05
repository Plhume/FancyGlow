package hhitt.fancyglow.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.PlayerGlowManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.nametag.NameTagManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class TabImplementation {

    private final FancyGlow plugin;
    private final YamlDocument configuration;
    private final PlayerGlowManager playerGlowManager;

    private String tabVersion;

    public TabImplementation(FancyGlow plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfiguration();
        this.playerGlowManager = plugin.getPlayerGlowManager();
    }

    public void initialize() {
        Plugin tabPlugin = plugin.getServer().getPluginManager().getPlugin("TAB");
        if (tabPlugin == null || !tabPlugin.isEnabled()) return;

        tabVersion = tabPlugin.getDescription().getVersion();
        if (!isCompatibleTAB(stripTags(tabVersion), "5.0.4")) {
            plugin.getLogger().warning("Any TAB implementation won't work due to version incompatibility.");
            plugin.getLogger().warning("You need at least version 5.0.4 or newer. Current version: " + tabVersion);
            return;
        }

        hook();
    }

    private void hook() {
        boolean autoTag = configuration.getBoolean("Auto_Tag", false);
        if (!autoTag) {
            plugin.getLogger().info("Compatible version of TAB has been found.");
            plugin.getLogger().info("You can enable the Auto_Tag option in your config to use it.");
        } else {
            plugin.getLogger().info("TAB " + tabVersion + " has been found, using it.");
        }

        TabAPI instance = TabAPI.getInstance();
        EventBus eventBus = Objects.requireNonNull(instance.getEventBus(), "TAB EventBus is not available.");

        eventBus.register(PlayerLoadEvent.class, event -> {
            if (!autoTag) return;

            if (!event.isJoin()) {
                applyTagPrefix(event, instance);
                return;
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> applyTagPrefix(event, instance), 15L);
        });
    }

    private void applyTagPrefix(PlayerLoadEvent event, TabAPI instance) {
        int ticks = plugin.getConfiguration().getInt("Rainbow_Update_Interval");
        if (ticks <= 0) {
            ticks = 1;
        }

        instance.getPlaceholderManager().registerPlayerPlaceholder(
                "%fancyglow_tab_color%",
                50 * ticks,
                player -> playerGlowManager.getPlayerGlowColor((Player) player.getPlayer()));

        NameTagManager nameTagManager = Objects.requireNonNull(instance.getNameTagManager(), "TAB NameTagManager is unavailable.");
        String originalPrefix = nameTagManager.getOriginalPrefix(event.getPlayer());

        if (originalPrefix == null) return;

        String modifiedPrefix = originalPrefix + "%fancyglow_tab_color%";
        nameTagManager.setPrefix(event.getPlayer(), modifiedPrefix);
    }


    private boolean isCompatibleTAB(String tabVersion, String desiredVersion) {
        if (tabVersion.equals(desiredVersion)) return true;

        try {
            String[] versionParts = tabVersion.split("\\.");
            String[] desiredVersionParts = desiredVersion.split("\\.");

            for (int i = 0; i < desiredVersionParts.length; i++) {
                int current = Integer.parseInt(versionParts[i]);
                int required = Integer.parseInt(desiredVersionParts[i]);

                if (current != required) return current > required;
            }

            return true;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            plugin.getLogger().warning("Failed to parse TAB version numbers: " + tabVersion);
            return false;
        }
    }

    public static String stripTags(final String version) {
        return version.replaceAll("[-;+].+", "");
    }
}
