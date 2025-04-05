package hhitt.fancyglow;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.Pattern;
import dev.dejvokep.boostedyaml.dvs.segment.Segment;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import hhitt.fancyglow.api.FancyGlowAPI;
import hhitt.fancyglow.api.FancyGlowAPIImpl;
import hhitt.fancyglow.inventory.CreatingInventory;
import hhitt.fancyglow.listeners.HeadClickListener;
import hhitt.fancyglow.listeners.MenuClickListener;
import hhitt.fancyglow.listeners.PlayerChangeWorldListener;
import hhitt.fancyglow.listeners.PlayerQuitListener;
import hhitt.fancyglow.managers.CommandLoader;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import hhitt.fancyglow.utils.FancyGlowPlaceholder;
import hhitt.fancyglow.utils.MessageHandler;
import hhitt.fancyglow.utils.MessageUtils;
import hhitt.fancyglow.utils.TabImplementation;
import hhitt.fancyglow.utils.UpdateChecker;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.checkerframework.checker.nullness.qual.NonNull;
import revxrsal.zapper.ZapperJavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public final class FancyGlow extends ZapperJavaPlugin {

    private static FancyGlowAPI API;
    private final Logger logger = super.getLogger();

    private BukkitAudiences adventure;

    private YamlDocument configuration;
    private MessageHandler messageHandler;

    private GlowManager glowManager;
    private PlayerGlowManager playerGlowManager;

    private CommandLoader commandLoader;
    private CreatingInventory inventory;

    public @NonNull BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            new Metrics(this, 22057);
            checkUpdates();

            new TabImplementation(this).initialize();
        });

        this.adventure = BukkitAudiences.create(this);
        MessageUtils.setAdventure(adventure());

        try {
            this.configuration = YamlDocument.create(
                    new File(this.getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new Pattern(Segment.range(1, Integer.MAX_VALUE)), "config-version").build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.messageHandler = new MessageHandler(this, configuration);

        this.glowManager = new GlowManager(this);
        this.playerGlowManager = new PlayerGlowManager(this);
        this.glowManager.scheduleFlashingTask();
        this.glowManager.scheduleMulticolorTask();
        this.inventory = new CreatingInventory(this);
        this.inventory.setupContent();

        API = new FancyGlowAPIImpl(this);
        getServer().getServicesManager().register(FancyGlowAPI.class, API, this, ServicePriority.Normal);

        this.commandLoader = new CommandLoader(this);

        registerEvents();

        hookPlaceholderAPI();
    }

    @Override
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        if (this.commandLoader != null) {
            this.commandLoader.unregisterAll();
        }

        if (this.glowManager != null) {
            this.glowManager.stopFlashingTask();
            this.glowManager.stopMulticolorTask();
        }
    }

    public void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new MenuClickListener(this), this);
        pluginManager.registerEvents(new HeadClickListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerChangeWorldListener(this), this);
    }

    private void checkUpdates() {
        if (!configuration.getBoolean("Notify_Updates")) return;
        UpdateChecker.init(this, 116326).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                this.logger.info(String.format("There is a new update available! FancyGlow %s may be downloaded on SpigotMC", result.getNewestVersion()));
                this.logger.info("Download it at https://www.spigotmc.org/resources/116326/updates");
            }
        });
    }

    private void hookPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            this.logger.warning("Could not find PlaceholderAPI!");
            this.logger.warning("This plugin is required if you want to use its placeholders.");
            return;
        }

        new FancyGlowPlaceholder(this).register();
    }

    public static FancyGlowAPI getAPI() {
        return API;
    }

    public YamlDocument getConfiguration() {
        return configuration;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public GlowManager getGlowManager() {
        return glowManager;
    }

    public PlayerGlowManager getPlayerGlowManager() {
        return playerGlowManager;
    }

    public CreatingInventory getInventory() {
        return inventory;
    }

    public void setInventory(CreatingInventory inventory) {
        this.inventory = inventory;
    }
}