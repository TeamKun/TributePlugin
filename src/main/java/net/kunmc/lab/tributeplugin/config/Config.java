package net.kunmc.lab.tributeplugin.config;

import net.kunmc.lab.configlib.BaseConfig;
import net.kunmc.lab.configlib.value.BooleanValue;
import net.kunmc.lab.configlib.value.UUIDValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Config extends BaseConfig {

  public final BooleanValue devMode = new BooleanValue(true);
  public final UUIDValue requester = new UUIDValue();

  public Config(@NotNull Plugin plugin) {
    super(plugin);
    super.loadConfig();
  }
}