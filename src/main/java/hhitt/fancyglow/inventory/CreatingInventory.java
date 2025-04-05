package hhitt.fancyglow.inventory;

import dev.dejvokep.boostedyaml.YamlDocument;
import hhitt.fancyglow.FancyGlow;
import hhitt.fancyglow.managers.GlowManager;
import hhitt.fancyglow.managers.PlayerGlowManager;
import hhitt.fancyglow.utils.ColorUtils;
import hhitt.fancyglow.utils.HeadUtils;
import hhitt.fancyglow.utils.MessageHandler;
import hhitt.fancyglow.utils.MessageUtils;
import hhitt.fancyglow.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DataFlowIssue")
public class CreatingInventory implements InventoryHolder {

    private static final String DEFAULT_RAINBOW_TEXTURE
            = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI0OTMyYmI5NDlkMGM2NTcxN2IxMjFjOGNkOWEyMWI2OWU4NmMwZjdlMzQyMWFlOWI4YzY0ZDhiOTkwZWI2MCJ9fX0";

    private final YamlDocument config;
    private final Inventory inventory;
    private final MessageHandler messageHandler;

    public CreatingInventory(FancyGlow plugin) {
        this.config = plugin.getConfiguration();
        this.messageHandler = plugin.getMessageHandler();
        this.inventory = plugin.getServer().createInventory(this, 6 * 9, messageHandler.getMessage(Messages.INVENTORY_TITLE));
    }

    public void setupContent() {
        setFiller();

        int i = 9;
        ItemStack colorItem;
        LeatherArmorMeta colorMeta;
        List<String> colorLoreMessage = messageHandler.getMessages(Messages.COLOR_LORE);
        for (ChatColor availableColor : GlowManager.COLORS_ARRAY) {
            colorItem = new ItemStack(Material.LEATHER_CHESTPLATE);
            colorMeta = (LeatherArmorMeta) colorItem.getItemMeta();

            colorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
            colorMeta.setLore(colorLoreMessage);
            Messages colorMessage = Messages.valueOf(availableColor.name().toUpperCase() + "_NAME");
            colorMeta.setDisplayName(messageHandler.getMessage(colorMessage));
            colorMeta.setColor(ColorUtils.getArmorColorFromChatColor(availableColor));
            colorItem.setItemMeta(colorMeta);

            if (i == 18) {
                i++;
            }
            inventory.setItem(i++, colorItem);
        }

        setRainbowItem();
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    private void setFiller() {
        if (!config.getBoolean("Inventory.Filler.Enabled")) return;

        Material material = Material.getMaterial(config.getString("Inventory.Filler.Material", "GRAY_STAINED_GLASS_PANE"));
        if (material == null) return;

        ItemStack fill = new ItemStack(material);
        ItemMeta fillMeta = fill.getItemMeta();
        fillMeta.setDisplayName(messageHandler.getMessage(Messages.FILLER_NAME));
        fill.setItemMeta(fillMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, fill);
        }
    }

    private void setRainbowItem() {
        ItemStack rainbowHead = HeadUtils.getCustomSkull(config.getString("Inventory.Rainbow.Texture", DEFAULT_RAINBOW_TEXTURE));
        ItemMeta rainbowHeadMeta = rainbowHead.getItemMeta();

        rainbowHeadMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rainbowHeadMeta.setDisplayName(messageHandler.getMessage(Messages.RAINBOW_HEAD_NAME));
        rainbowHeadMeta.setLore(messageHandler.getMessages(Messages.RAINBOW_HEAD_LORE));
        rainbowHead.setItemMeta(rainbowHeadMeta);

        inventory.setItem(config.getInt("Inventory.Rainbow.Slot", 40), rainbowHead);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
