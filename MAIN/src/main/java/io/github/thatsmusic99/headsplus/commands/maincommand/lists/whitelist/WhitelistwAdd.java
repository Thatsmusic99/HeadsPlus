package io.github.thatsmusic99.headsplus.commands.maincommand.lists.whitelist;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.commands.CommandInfo;
import io.github.thatsmusic99.headsplus.commands.maincommand.lists.AbstractListAdd;

import java.util.List;

@CommandInfo(
        commandname = "whitelistwadd",
        permission = "headsplus.maincommand.whitelistw.add",
        subcommand = "Whitelistwadd",
        maincommand = true,
        usage = "/hp whitelistwadd <World Name>"
)
public class WhitelistwAdd extends AbstractListAdd {

    @Override
    public String getCmdDescription() {
        return HeadsPlus.getInstance().getMessagesConfig().getString("descriptions.hp.whitelistwadd");
    }

    @Override
    public List<String> getList() {
        return config.getWorldWhitelist().list;
    }

    @Override
    public String getPath() {
        return "whitelist.world.list";
    }

    @Override
    public String getListType() {
        return "wl";
    }

    @Override
    public String getType() {
        return "world";
    }

    @Override
    public String getFullName() {
        return "whitelist";
    }

}

