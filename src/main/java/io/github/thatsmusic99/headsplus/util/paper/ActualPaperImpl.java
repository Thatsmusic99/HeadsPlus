package io.github.thatsmusic99.headsplus.util.paper;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.github.thatsmusic99.headsplus.HeadsPlus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ActualPaperImpl implements PaperImpl {

    private static final Executor asyncExecutor = task -> Bukkit.getScheduler().runTaskAsynchronously(HeadsPlus.getInstance(), task);
    private static final Executor syncExecutor = task -> Bukkit.getScheduler().runTask(HeadsPlus.getInstance(), task);

    @Override
    public CompletableFuture<SkullMeta> setProfile(SkullMeta meta, String name) {
        return CompletableFuture.supplyAsync(() -> {
            UUID uuid;
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                uuid = player.getUniqueId();
            } else {
                uuid = UUID.nameUUIDFromBytes(name.getBytes());
            }

            try {
                PlayerProfile profile = Bukkit.getServer().createProfile(uuid, name);
                profile.complete();
                meta.setPlayerProfile(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return meta;
        }, asyncExecutor).thenApplyAsync(sm -> sm, syncExecutor);
    }
}
