package io.github.thatsmusic99.headsplus.listeners;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.config.HeadsPlusConfigHeads;
import io.github.thatsmusic99.headsplus.config.HeadsPlusMainConfig;
import io.github.thatsmusic99.headsplus.nms.NMSIndex;
import io.github.thatsmusic99.headsplus.util.EntityDataManager;
import io.github.thatsmusic99.headsplus.util.FlagHandler;
import io.github.thatsmusic99.headsplus.util.HPUtils;
import io.github.thatsmusic99.headsplus.util.events.HeadsPlusEventExecutor;
import io.github.thatsmusic99.headsplus.util.events.HeadsPlusListener;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HPEntityDeathEvent extends HeadsPlusListener<EntityDeathEvent> {

    private final HeadsPlusConfigHeads hpch = HeadsPlus.getInstance().getHeadsConfig();

    public HPEntityDeathEvent() {
        super();
        Bukkit.getPluginManager().registerEvent(EntityDeathEvent.class,
                this, EventPriority.NORMAL,
                new HeadsPlusEventExecutor(EntityDeathEvent.class, "EntityDeathEvent", this), HeadsPlus.getInstance());

        int length = EntityDataManager.ableEntities.size();
        String[] entities = new String[length];
        for (int i = 0; i < length; i++) {
            entities[i] = EntityDataManager.ableEntities.get(i);
        }
        addPossibleData("entity-type", entities);

        for (String key : Arrays.asList("enabled", "is-mythic-mob", "not-wg-restricted")) {
            addPossibleData(key, "true", "false");
        }

        length = CreatureSpawnEvent.SpawnReason.values().length;
        String[] reasons = new String[length];
        for (int i = 0; i < length; i++) {
            reasons[i] = CreatureSpawnEvent.SpawnReason.values()[i].name();
        }
        addPossibleData("spawn-cause", reasons);

        addPossibleData("fixed-chance", "<double>");
        addPossibleData("random-chance", "<double>");
        List<String> metadata = new ArrayList<>();
        NMSIndex index = HeadsPlus.getInstance().getNMSVersion();
        if (index.getOrder() < 6) {
            for (Horse.Variant variant : Horse.Variant.values()) {
                HPUtils.addIfAbsent(metadata, variant.name());
            }
        }
        for (Horse.Color color : Horse.Color.values()) {
            HPUtils.addIfAbsent(metadata, color.name());
        }
        for (DyeColor color : DyeColor.values()) {
            HPUtils.addIfAbsent(metadata, color.name());
        }
        for (Rabbit.Type type : Rabbit.Type.values()) {
            HPUtils.addIfAbsent(metadata, type.name());
        }
        if (index.getOrder() > 8) {
            for (TropicalFish.Pattern pattern : TropicalFish.Pattern.values()) {
                HPUtils.addIfAbsent(metadata, pattern.name());
            }
        }
        metadata.add("SNOW");
        if (index.getOrder() > 10) {
            for (Cat.Type type : Cat.Type.values()) {
                HPUtils.addIfAbsent(metadata, type.name());
            }
        } else {
            for (Ocelot.Type type : Ocelot.Type.values()) {
                HPUtils.addIfAbsent(metadata, type.name());
            }
        }


        addPossibleData("metadata", "default",
                "WHITE", "CREAMY", "CHESTNUT", "BROWN", "BLACK", "GRAY", "DARK_BROWN",
                "RED", "ORANGE", "YELLOW", "LIME", "GREEN", "LIGHT_BLUE", "CYAN", "BLUE", "PURPLE", "MAGENTA", "PINK", "LIGHT_GRAY",
                "BLACK_AND_WHITE", "GOLD", "SALT_AND_PEPPER", "THE_KILLER_BUNNY",
                "KOB", "SUNSTREAK", "SNOOPER", "DASHER", "BRINELY", "SPOTTY", "FLOPPER", "STRIPEY", "GLITTER", "BLOCKFISH", "BETTY", "CLAYFISH",
                "SNOW",
                "TABBY", "SIAMESE", "BRITISH_SHORTHAIR", "CALICO", "PERSIAN", "RAGDOLL", "JELLIE", "ALL_BLACK",
                "NONE", "");

        addPossibleData("killer", "<Player>");
    }

    @Override
    public void onEvent(EntityDeathEvent event) {
        addData("entity-type", event.getEntityType().name());
        addData("killer", event.getEntity().getKiller() == null ? "<None>" : event.getEntity().getKiller().getName());
        // Make sure head drops are enabled
        if (!addData("enabled", hp.isDropsEnabled())) return;
        // Make sure the entity is valid
        if (!EntityDataManager.ableEntities.contains(event.getEntityType().name())) return;
        // Make sure the entity isn't from MythicMobs
        if (addData("is-mythic-mob", HPUtils.isMythicMob(event.getEntity()))) return;
        // And make sure there is no WG region saying no
        // I SWEAR TO GOD WORLDGUARD IS SUCH A BRAT
        if (!addData("not-wg-restricted", !hp.canUseWG() || FlagHandler.canDrop(event.getEntity().getLocation(), event.getEntity().getType()))) return;
        // TODO New blacklist checks go here
        if (!HPUtils.runBlacklistTests(event.getEntity())) return;
        //
        if (addData("spawn-cause", HPEntitySpawnEvent.getReason(event.getEntity().getUniqueId())) != null) {
            if (hp.getConfiguration().getMechanics().getStringList("blocked-spawn-causes").contains(getData("spawn-cause"))) {
                return;
            }
        }
        String entity;
        switch (event.getEntityType().name()) {
            case "WANDERING_TRADER":
            case "TRADER_LLAMA":
                entity = event.getEntityType().name().toLowerCase();
                break;
            default:
                entity = event.getEntityType().name().toLowerCase().replaceAll("_", "");
        }
        double fixedChance = addData("fixed-chance", hpch.getChance(entity));
        if (fixedChance == 0) return;
        double randomChance = addData("random-chance", new Random().nextDouble() * 100);
        if (event.getEntity().getKiller() != null) {
            fixedChance = HPUtils.calculateChance(fixedChance, randomChance, event.getEntity().getKiller());
        }
        if (randomChance <= fixedChance) {
            String meta = addData("metadata", EntityDataManager.getMeta(event.getEntity()));
            int amount = addData("amount", HPUtils.getAmount(fixedChance));
            HPUtils.dropHead(event.getEntityType().name(), meta, event.getEntity().getLocation(), amount, event.getEntity().getKiller());
        }
    }



}
