package hhitt.fancyglow.utils;

import hhitt.fancyglow.FancyGlow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

public class HeadUtils {
    private static final Logger LOGGER = FancyGlow.getPlugin(FancyGlow.class).getLogger();

    /**
     * @param base64 Base64 string to use as texture.
     *
     * @return A head item stack with the specified texture.
     */
    public static ItemStack getCustomSkull(String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (base64 == null || base64.isEmpty()) {
            return skull;
        }
        URL urlObject;
        try {
            urlObject = getUrlFromBase64(base64);
        } catch (MalformedURLException e) {
            LOGGER.warning("Unexpected exception while trying to get textured head with the following message: " + e.getMessage());
            return skull;
        }
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "FancyCustomHead");
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(urlObject);
        profile.setTextures(textures);

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwnerProfile(profile);
        skull.setItemMeta(meta);

        return skull;
    }

    /**
     * @param base64 Base64 string.
     *
     * @return URL object to use as skin.
     * @throws MalformedURLException
     */
    public static URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }
}
