package net.kunmc.lab.tributeplugin.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class CommonUtil {

  public static String getNameSpace(JavaPlugin plugin) {
    return new NamespacedKey(plugin, "a").getNamespace();
  }
}
