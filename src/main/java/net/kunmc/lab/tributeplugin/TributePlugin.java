package net.kunmc.lab.tributeplugin;

import net.kunmc.lab.commandlib.CommandLib;
import net.kunmc.lab.configlib.ConfigCommandBuilder;
import net.kunmc.lab.tributeplugin.command.MainCommand;
import net.kunmc.lab.tributeplugin.command.RequestCommand;
import net.kunmc.lab.tributeplugin.config.Config;
import net.kunmc.lab.tributeplugin.util.CommonUtil;
import net.kunmc.lab.tributeplugin.util.bossbar.BossBarUtil;
import net.kunmc.lab.tributeplugin.util.task.QueuedExecutor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TributePlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    Store.pluginName = this.getName();
    Store.plugin = this;
    Store.config = new Config(this);
    CommandLib.register(this,
        new MainCommand("tribute", new ConfigCommandBuilder(Store.config).build()),
        new RequestCommand("request"));
    Store.queuedExecutor = new QueuedExecutor();
    Bukkit.getPluginManager().registerEvents(Store.queuedExecutor, this);
  }

  @Override
  public void onDisable() {
    Store.config.requester.toPlayer().setGlowing(false);
    BossBarUtil.removeBossBars(CommonUtil.getNameSpace(this));
  }
}
