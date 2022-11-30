package net.kunmc.lab.tributeplugin.config;

import net.kunmc.lab.configlib.BaseConfig;
import net.kunmc.lab.configlib.value.BooleanValue;
import net.kunmc.lab.configlib.value.IntegerValue;
import net.kunmc.lab.configlib.value.UUIDValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Config extends BaseConfig {

  public final BooleanValue devMode = new BooleanValue(false).writableByCommand(false);
  public final UUIDValue requester = new UUIDValue();
  public final IntegerValue blackHaleRadius = new IntegerValue(7);

  public Config(@NotNull Plugin plugin) {
    super(plugin);
    super.loadConfig();
  }
}
