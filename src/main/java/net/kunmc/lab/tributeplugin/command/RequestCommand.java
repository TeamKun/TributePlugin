package net.kunmc.lab.tributeplugin.command;

import net.kunmc.lab.commandlib.Command;
import net.kunmc.lab.tributeplugin.request.Request;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RequestCommand extends Command {

  public RequestCommand(@NotNull String name) {
    super(name);
    argument(argumentBuilder -> {
      argumentBuilder.itemStackArgument("item")
          .integerArgument("amount", 0, 100000)
          .integerArgument("timelimit")
          .execute(
              commandContext -> {
                Material target = ((ItemStack) commandContext.getParsedArg("item")).getType();
                int amount = (int) commandContext.getParsedArg("amount");
                int timeLimit = (int) commandContext.getParsedArg("timelimit");
                new Request(target, amount, timeLimit);
              }
          );
    });
  }
}
