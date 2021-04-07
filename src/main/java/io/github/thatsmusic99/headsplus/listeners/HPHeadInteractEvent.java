package io.github.thatsmusic99.headsplus.listeners;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.config.ConfigInteractions;
import io.github.thatsmusic99.headsplus.config.ConfigMobs;
import io.github.thatsmusic99.headsplus.config.HeadsPlusMessagesManager;
import io.github.thatsmusic99.headsplus.util.events.HeadsPlusEventExecutor;
import io.github.thatsmusic99.headsplus.util.events.HeadsPlusListener;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

public class HPHeadInteractEvent extends HeadsPlusListener<PlayerInteractEvent> {

    private final HeadsPlusMessagesManager hpc = HeadsPlus.getInstance().getMessagesConfig();
    private final List<UUID> sent = new ArrayList<>();

    public HPHeadInteractEvent() {
        super();
        Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, this, EventPriority.NORMAL,
                new HeadsPlusEventExecutor(PlayerInteractEvent.class, "HPHeadInteractEvent", this), HeadsPlus.getInstance());

        int length = Action.values().length;
        String[] actions = new String[length];
        for (int i = 0; i < length; i++) {
            actions[i] = Action.values()[i].name();
        }
        addPossibleData("action", actions);
        addPossibleData("is-skull", "true", "false");
        addPossibleData("owner", "<Name>");
    }

    // TODO - rewrite for interactions overhaul
    @Override
    public void onEvent(PlayerInteractEvent event) {
        try {
            if (addData("action", event.getAction()) == Action.RIGHT_CLICK_BLOCK) {
                if (HeadsPlus.getInstance().getConfiguration().getPerks().click_in) {
                    Player player = event.getPlayer();
                    BlockState block = event.getClickedBlock().getState();
                    if (addData("is-skull", block instanceof Skull)) {

                        Skull skull = (Skull) block;
                        String owner;

                        owner = addData("owner", skull.getOwner());
                        if (owner == null) return;
                        String playerName = player.getName();
                        ConfigMobs hpch = HeadsPlus.getInstance().getHeadsConfig();
                        List<String> names = new ArrayList<>();
                        names.addAll(hpch.eHeads);
                        names.addAll(hpch.ieHeads);
                        if (!sent.contains(player.getUniqueId())) {
                            sent.add(player.getUniqueId());
                            ConfigInteractions.get().getMessageForHead(skull, player).thenAccept(player::sendMessage);
                        } else {
                            sent.remove(player.getUniqueId());
                        }
                    }
                }

            }
        } catch (NullPointerException ex) {
            //
        }
    }
}
