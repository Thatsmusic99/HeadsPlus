package io.github.thatsmusic99.headsplus.api;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.api.events.LevelUpEvent;
import io.github.thatsmusic99.headsplus.config.HeadsPlusConfigHeads;
import io.github.thatsmusic99.headsplus.storage.PlayerScores;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HPPlayer {

    // M
    private UUID player;
    private int xp;
    private Level level = null;
    private List<String> completeChallenges;
    private Level nextLevel = null;
    private String currentMask;
    private List<PotionEffect> activeMask;
    public static HashMap<UUID, HPPlayer> players = new HashMap<>();
    private List<String> favouriteHeads;
    private List<String> pinnedChallenges;
    private boolean ignoreFallDamage;
    private String cachedLocale;
    private boolean localeForced;

    public HPPlayer(OfflinePlayer p) {
        if (p.isOnline() && p.getPlayer().hasMetadata("NPC")) return;
        HeadsPlus hp = HeadsPlus.getInstance();
        activeMask = new ArrayList<>();
        favouriteHeads = new ArrayList<>();
        pinnedChallenges = new ArrayList<>();
        ignoreFallDamage = false;
        this.player = p.getUniqueId();
        try {
            for (Object o : (JSONArray) hp.getFavourites().getJSON().get(p.getUniqueId().toString())) {
                favouriteHeads.add(String.valueOf(o));
            }
            for (Object o : (JSONArray) hp.getPinned().getJSON().get(player.toString())) {
                pinnedChallenges.add(String.valueOf(o));
            }
        } catch (NullPointerException ignored) {
        }
        PlayerScores scores = hp.getScores();
        HashMap<Integer, Level> levels = hp.getLevels();
        this.xp = scores.getXp(p.getUniqueId().toString());
        List<String> sc = new ArrayList<>();
        sc.addAll(scores.getCompletedChallenges(p.getUniqueId().toString()));
        if (hp.getConfiguration().getConfig().getBoolean("smart-locale") && p.isOnline()) {
            Player player = p.getPlayer();
            String loc = scores.getLocale(p.getUniqueId().toString());
            if (loc != null && !loc.isEmpty() && !loc.equalsIgnoreCase("null")) {
                cachedLocale = loc.split(":")[0];
                localeForced = Boolean.parseBoolean(loc.split(":")[1]);
                hp.getMessagesConfig().setPlayerLocale(player, cachedLocale,  false);
            } else {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hp.getMessagesConfig().setPlayerLocale(player);
                        cachedLocale = hp.getMessagesConfig().getSetLocale(player);
                        localeForced = false;
                        scores.setLocale(player.toString(), cachedLocale, false);
                    }
                }.runTaskLaterAsynchronously(hp, 100);
            }
        } else {
            cachedLocale = "";
            localeForced = false;
        }
        if (hp.usingLevels()) {
            if (scores.getLevel(p.getUniqueId().toString()).isEmpty()) {
                for (int i = hp.getLevelsConfig().getMaxHierarchy(); i > 0; i--) {
                    try {
                        if (levels.get(i) != null && levels.get(i).getRequiredXP() <= xp) {
                            level = levels.get(i);

                            scores.setLevel(p.getUniqueId().toString(), level.getConfigName());
                            try {
                                for (int j = i + 1; j < hp.getLevelsConfig().getMaxHierarchy(); j++) {
                                    if (levels.get(j) != null) {
                                        nextLevel = levels.get(j);
                                        break;
                                    }
                                }
                            } catch (IndexOutOfBoundsException e) { // End of levels
                                nextLevel = null;
                            }
                            break;
                        }
                    } catch (NullPointerException ignored) { // Continue
                    }

                }
            } else {
                String configLevel = scores.getLevel(p.getUniqueId().toString());
                for (int i = hp.getLevelsConfig().getMaxHierarchy(); i > 0; i--) {
                    try {
                        if (levels.get(i) != null && levels.get(i).getConfigName().equals(configLevel)) {
                            level = levels.get(i);
                            try {
                                for (int j = i + 1; j < hp.getLevelsConfig().getMaxHierarchy(); j++) {
                                    if (levels.get(j) != null) {
                                        nextLevel = levels.get(j);
                                        break;
                                    }
                                }
                            } catch (IndexOutOfBoundsException e) { // End of levels
                                nextLevel = null;
                            }
                            break;
                        }
                    } catch (NullPointerException ignored) {

                    }

                }
            }
        }
        this.completeChallenges = sc;
        players.put(p.getUniqueId(), this);
    }

    public void clearMask() {
        try {
            ignoreFallDamage = false;
            currentMask = "";
            if (!getPlayer().isOnline()) return;
            for (PotionEffect p : activeMask) {
                ((Player) getPlayer()).removePotionEffect(p.getType());
            }
            activeMask.clear();
        } catch (NullPointerException ignored) { // When the mask messes up

        }
    }

    public void tempClearMasks() {
        try {
            ignoreFallDamage = false;
            if (!getPlayer().isOnline()) return;
            for (PotionEffect p : activeMask) {
                ((Player) getPlayer()).removePotionEffect(p.getType());
            }
        } catch (NullPointerException ignored) {
        }
    }

    public void addMask(String s) {
        if (s.equalsIgnoreCase("WANDERING_TRADER") || s.equalsIgnoreCase("TRADER_LLAMA")) {
            s = s.toLowerCase();
        } else {
            s = s.toLowerCase().replaceAll("_", "");
        }
        HeadsPlusConfigHeads hpch = HeadsPlus.getInstance().getHeadsConfig();
        List<PotionEffect> po = new ArrayList<>();
        for (int i = 0; i < hpch.getConfig().getStringList(s + ".mask-effects").size(); i++) {
            String is = hpch.getConfig().getStringList(s + ".mask-effects").get(i).toUpperCase();
            if (is.equalsIgnoreCase("ignore-fall-damage")) {
                ignoreFallDamage = true;
                continue;
            }
            int amp;
            if (ignoreFallDamage) {
                amp = hpch.getConfig().getIntegerList(s + ".mask-amplifiers").get(i - 1);
            } else {
                amp = hpch.getConfig().getIntegerList(s + ".mask-amplifiers").get(i);
            }

            try {
                PotionEffectType type = PotionEffectType.getByName(is);
                if (type == null) {
                    HeadsPlus.getInstance().getLogger().severe("Invalid potion type detected. Please check your masks configuration in heads.yml! (" + is + ", " + s + ")");
                    continue;
                }
                PotionEffect p = new PotionEffect(type, HeadsPlus.getInstance().getConfiguration().getMechanics().getInt("masks.effect-length"), amp);
                ((Player) getPlayer()).addPotionEffect(p);
                po.add(p);
            } catch (IllegalArgumentException ex) {
                HeadsPlus.getInstance().getLogger().severe("Invalid potion type detected. Please check your masks configuration in heads.yml!");
            }
        }
        activeMask = po;
        currentMask = s;
    }

    public void refreshMasks() {
        for (PotionEffect effect : activeMask) {
            Player player = (Player) getPlayer();
            player.removePotionEffect(effect.getType());
            player.addPotionEffect(new PotionEffect(effect.getType(), HeadsPlus.getInstance().getConfiguration().getMechanics().getInt("masks.effect-length"), effect.getAmplifier()));
        }
    }

    public void clearAllMasks() {
        for (PotionEffect effect : activeMask) {
            Player player = (Player) getPlayer();
            player.removePotionEffect(effect.getType());
        }
    }

    public int getXp() {
        return xp;
    }

    public Level getLevel() {
        return level;
    }

    public Level getNextLevel() {
        return nextLevel;
    }

    public List<String> getCompleteChallenges() {
        return completeChallenges;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }

    public static HPPlayer getHPPlayer(OfflinePlayer p) {
        UUID uuid = p.getUniqueId();
        return players.get(uuid) != null ? players.get(uuid) : new HPPlayer(p);
    }

    public List<PotionEffect> getActiveMasks() {
        return activeMask;
    }

    public String getActiveMaskType() {
        return currentMask;
    }

    public String getLocale() {
        return cachedLocale;
    }

    public boolean isLocaleForced() {
        return localeForced;
    }

    public void setLocale(String locale) {
        setLocale(locale, true);
    }

    public void setLocale(String locale, boolean forced) {
        cachedLocale = locale;
        localeForced = forced;
        HeadsPlus.getInstance().getScores().setLocale(getPlayer().getUniqueId().toString(), locale, forced);
    }

    public void addCompleteChallenge(Challenge c) {
        PlayerScores scores = HeadsPlus.getInstance().getScores();
        scores.completeChallenge(player.toString(), c);
        completeChallenges.add(c.getConfigName());
    }

    public void addXp(int xp) {
        setXp(this.xp + xp);
    }

    public void removeXp(int xp) {
        setXp(this.xp - xp);
    }

    public void setXp(int xp) {
        HeadsPlus hp = HeadsPlus.getInstance();
        PlayerScores scores = HeadsPlus.getInstance().getScores();
        scores.setXp(player.toString(), xp);
        this.xp = xp;
            if (hp.usingLevels()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        while (nextLevel != null && nextLevel.getRequiredXP() <= getXp()) {
                            LevelUpEvent event = new LevelUpEvent((Player) getPlayer(), level, nextLevel);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                level = nextLevel;
                                Player player = (Player) getPlayer();
                                if (hp.getConfiguration().getMechanics().getBoolean("broadcasts.level-up")) {
                                    final String name = player.isOnline() ? player.getPlayer().getDisplayName() : player.getName();
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        hp.getMessagesConfig().sendMessage("commands.levels.level-up", p, "{player}", name, "{name}", name, "{level}", ChatColor.translateAlternateColorCodes('&', level.getDisplayName()));
                                    }
                                }
                                HashMap<Integer, Level> levels = HeadsPlus.getInstance().getLevels();
                                scores.setLevel(player.getUniqueId().toString(), level.getConfigName());
                                if (level.isrEnabled()) {
                                    level.getReward().reward(player.getPlayer());
                                }
                                for (int i = 1; i < hp.getLevelsConfig().getMaxHierarchy(); i++) {
                                    if (levels.get(i) == level) {
                                        try {
                                            level = levels.get(i);
                                            boolean found = false;
                                            for (int j = i + 1; j < hp.getLevelsConfig().getMaxHierarchy(); j++) {
                                                if (levels.get(j) != null) {
                                                    nextLevel = levels.get(j);
                                                    found = true;
                                                    break;
                                                }
                                            }
                                            if (!found) {
                                                nextLevel = null;
                                            } else {
                                                break;
                                            }
                                        } catch (IndexOutOfBoundsException e) { // End of levels
                                            nextLevel = null;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.runTask(hp);
                if (level == null || level.getRequiredXP() > getXp()) {
                    HashMap<Integer, Level> levels = hp.getLevels();
                    for (int i = 1; i < hp.getLevelsConfig().getMaxHierarchy(); i++) {
                        if (levels.get(i) != null && levels.get(i).getRequiredXP() <= getXp()) {
                            try {
                                level = levels.get(i);
                                boolean found = false;
                                for (int j = i + 1; j < hp.getLevelsConfig().getMaxHierarchy(); j++) {
                                    if (levels.get(i) != null) {
                                        nextLevel = levels.get(i + 1);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) nextLevel = null;
                                return;
                            } catch (IndexOutOfBoundsException e) { // End of levels
                                nextLevel = null;
                            }
                        }
                    }
                    level = null;
                    nextLevel = levels.get(1);
                }
            }
    }

    public boolean hasHeadFavourited(String s) {
        return favouriteHeads.contains(s);
    }

    public void addFavourite(String s) {
        favouriteHeads.add(s);
        HeadsPlus.getInstance().getFavourites().writeData(getPlayer(), s);
    }

    public void removeFavourite(String s) {
        favouriteHeads.remove(s);
        HeadsPlus.getInstance().getFavourites().removeHead(getPlayer(), s);
    }

    public boolean hasChallengePinned(Challenge challenge) {
        return pinnedChallenges.contains(challenge.getConfigName());
    }

    public void addChallengePin(Challenge challenge) {
        String s = challenge.getConfigName();
        pinnedChallenges.add(s);
        HeadsPlus.getInstance().getPinned().writeData(getPlayer(), s);
    }

    public void removeChallengePin(Challenge challenge) {
        String s = challenge.getConfigName();
        pinnedChallenges.remove(s);
        HeadsPlus.getInstance().getPinned().removeChallenge(getPlayer(), s);
    }

    public List<String> getPinnedChallenges() {
        return pinnedChallenges;
    }

    public boolean isIgnoringFallDamage() {
        return ignoreFallDamage;
    }

    public List<String> getFavouriteHeads() {
        return favouriteHeads;
    }
}
