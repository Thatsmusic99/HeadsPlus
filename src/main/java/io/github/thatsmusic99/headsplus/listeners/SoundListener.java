package io.github.thatsmusic99.headsplus.listeners;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.config.ConfigSounds;
import io.github.thatsmusic99.headsplus.util.events.HeadsPlusListener;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SoundListener<T> extends HeadsPlusListener<T> {

    private String section;
    private String playerAccessor;

    public SoundListener(String section) {
        this.section = section;
        this.playerAccessor = "getPlayer";
    }

    public SoundListener(String section, String playerAccessor) {
        this.section = section;
        this.playerAccessor = playerAccessor;
    }

    @Override
    public void onEvent(T event) {
        if (ConfigSounds.get().getBoolean("sounds." + section + ".enabled")) {
            try {
                Sound s = Sound.valueOf(ConfigSounds.get().getString("sounds." + section + ".sound"));
                float vol = (float) ConfigSounds.get().getDouble("sounds." + section + ".volume");
                float pitch = (float) ConfigSounds.get().getDouble("sounds." +section + ".pitch");
                Player player = getPlayer(event);
                if (player == null) return;
                player.playSound(player.getLocation(), s, vol, pitch);
            } catch (IllegalArgumentException ex) {
                HeadsPlus.get().getLogger().warning("Could not find sound " + ConfigSounds.get().getString("sounds." + section + ".sound") + "! (Error code: 7)");
            }
        }
    }

    private Player getPlayer(T event) {
        try {
            Method playerMethod = event.getClass().getDeclaredMethod(playerAccessor);
            return (Player) playerMethod.invoke(event);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void init() {

    }
}