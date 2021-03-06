package io.github.thatsmusic99.headsplus.listeners;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.api.events.*;
import io.github.thatsmusic99.headsplus.config.HeadsPlusConfigSounds;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SoundEvent implements Listener {

    private final HeadsPlusConfigSounds sounds = HeadsPlus.getInstance().getSounds();

    @EventHandler
    public void onHeadSell(SellHeadEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-sell-head.enabled")) {
            playSound(event.getPlayer(), "on-sell-head");
        }
    }

    @EventHandler
    public void onHeadBuy(HeadPurchaseEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-buy-head.enabled")) {
            playSound(event.getPlayer(), "on-buy-head");
        }
    }

    @EventHandler
    public void onSectionChange(SectionChangeEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-change-section.enabled")) {
            playSound(event.getPlayer(), "on-change-section");
        }
    }

    @EventHandler
    public void onEntityHeadDrop(EntityHeadDropEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-entity-head-drop.enabled")) {
            if (event.getPlayer() != null) {
                playSound(event.getPlayer(), "on-entity-head-drop");
            }
        }
    }

    @EventHandler
    public void onPlayerHeadDrop(PlayerHeadDropEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-player-head-drop.enabled")) {
            playSound(event.getDeadPlayer(), "on-player-head-drop");
        }
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-level-up.enabled")) {
            playSound(event.getPlayer(), "on-level-up");
        }
    }

    @EventHandler
    public void onHeadCraft(HeadCraftEvent event) {
        if (sounds.getConfig().getBoolean("sounds.on-craft-head.enabled")) {
            playSound(event.getPlayer(), "on-craft-head");
        }
    }

    private void playSound(Player player, String st) {
        try {
            Sound s = Sound.valueOf(sounds.getConfig().getString("sounds." + st + ".sound"));
            float vol = (float) sounds.getConfig().getDouble("sounds." + st + ".volume");
            float pitch = (float) sounds.getConfig().getDouble("sounds." + st + ".pitch");
            player.playSound(player.getLocation(), s, vol, pitch);
        } catch (IllegalArgumentException ex) {
            HeadsPlus.getInstance().getLogger().warning("Could not find sound " + sounds.getConfig().getString("sounds." + st + ".sound") + "! (Error code: 7)");
        }

    }
}
