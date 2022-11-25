package net.kunmc.lab.tributeplugin;

import net.kunmc.lab.tributeplugin.config.Config;
import net.kunmc.lab.tributeplugin.util.task.QueuedExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class Store {

  public static String pluginName;
  public static JavaPlugin plugin;
  public static QueuedExecutor queuedExecutor;
  public static Config config;
}
