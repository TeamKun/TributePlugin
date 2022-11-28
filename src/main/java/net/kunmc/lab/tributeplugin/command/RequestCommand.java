package net.kunmc.lab.tributeplugin.command;

import net.kunmc.lab.commandlib.Command;
import org.jetbrains.annotations.NotNull;

public class RequestCommand extends Command {

  public RequestCommand(@NotNull String name) {
    super(name);
    addChildren(new RequestStart("start"), new RequestCancel("cancel"));
  }
}
