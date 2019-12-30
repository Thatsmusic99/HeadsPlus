package io.github.thatsmusic99.headsplus.commands.maincommand.lists.blacklist;

import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.commands.CommandInfo;
import io.github.thatsmusic99.headsplus.commands.maincommand.lists.AbstractListAdd;

import java.util.List;

@CommandInfo(
        commandname = "blacklistwadd",
        permission = "headsplus.maincommand.blacklistw.add",
        subcommand = "Blacklistwadd",
        maincommand = true,
        usage = "/hp blacklistwadd <World Name>"
)
public class BlacklistwAdd extends AbstractListAdd {

    // I

	@Override
	public String getCmdDescription() {
		return HeadsPlus.getInstance().getMessagesConfig().getString("descriptions.hp.blacklistwadd");
	}

    @Override
    public List<String> getList() {
        return config.getWorldBlacklist().list;
    }

    @Override
    public String getPath() {
        return "blacklist.world.list";
    }

    @Override
    public String getListType() {
        return "bl";
    }

    @Override
    public String getType() {
        return "world";
    }

    @Override
    public String getFullName() {
        return "blacklist";
    }

}
