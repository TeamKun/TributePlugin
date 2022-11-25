package net.kunmc.lab.tributeplugin;

import net.kunmc.lab.commandlib.CommandLib;
import net.kunmc.lab.configlib.ConfigCommandBuilder;
import net.kunmc.lab.tributeplugin.command.MainCommand;
import net.kunmc.lab.tributeplugin.config.Config;
import net.kunmc.lab.tributeplugin.util.task.QueuedExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TributePlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Store.pluginName = this.getName();
    Store.plugin = this;
    Store.config = new Config(this);
    CommandLib.register(this,
        new MainCommand("tribute", new ConfigCommandBuilder(Store.config).build()));
    Store.queuedExecutor = new QueuedExecutor();
    Bukkit.getPluginManager().registerEvents(Store.queuedExecutor, this);
  }

  @Override
  public void onDisable() {
    Bukkit.getBossBars().remove();
  }
}
