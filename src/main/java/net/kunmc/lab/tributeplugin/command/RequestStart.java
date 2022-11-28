package net.kunmc.lab.tributeplugin.command;

import net.kunmc.lab.commandlib.Command;
import net.kunmc.lab.tributeplugin.Store;
import net.kunmc.lab.tributeplugin.request.RequestManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RequestStart extends Command {

  public RequestStart(@NotNull String name) {
    super(name);
    argument(argumentBuilder -> {
      argumentBuilder.itemStackArgument("item")
          .integerArgument("amount", 0, 100000)
          .integerArgument("timelimit")
          .execute(
              commandContext -> {
                if (Store.config.requester.toPlayer() == null) {
                  commandContext.sendFailure("requesterが設定されていないかサーバーに存在しません");
                  return;
                }
                Material target = ((ItemStack) commandContext.getParsedArg("item")).getType();
                int amount = (int) commandContext.getParsedArg("amount");
                int timeLimit = (int) commandContext.getParsedArg("timelimit");
                RequestManager.start(target, amount, timeLimit);
              }
          );
    });
  }
}
