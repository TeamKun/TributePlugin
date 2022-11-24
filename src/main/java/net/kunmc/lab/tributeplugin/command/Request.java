package net.kunmc.lab.tributeplugin.command;

import net.kunmc.lab.commandlib.Command;
import net.kunmc.lab.tributeplugin.util.message.MessageUtil;
import net.kunmc.lab.tributeplugin.util.timer.DisplayType;
import net.kunmc.lab.tributeplugin.util.timer.EndProcess;
import net.kunmc.lab.tributeplugin.util.timer.RegularProcess;
import net.kunmc.lab.tributeplugin.util.timer.Timer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Request extends Command {

  public Request(@NotNull String name) {
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
                new Timer(timeLimit)
                    .setRegularProcess(new RegularProcess() {
                      @Override
                      public boolean execute() {
                        MessageUtil.broadcast("regular");
                        return false;
                      }
                    })
                    .setEndProcess(new EndProcess() {
                      @Override
                      public void execute() {
                        MessageUtil.broadcast("end");
                      }
                    })
                    .setDisplayType(DisplayType.BOSSBAR)
                    .start();
              }
          );
    });
  }
}
