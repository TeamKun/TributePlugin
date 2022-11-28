package net.kunmc.lab.tributeplugin.command;

import net.kunmc.lab.commandlib.Command;
import net.kunmc.lab.tributeplugin.request.RequestManager;
import org.jetbrains.annotations.NotNull;

public class RequestCancel extends Command {

  public RequestCancel(@NotNull String name) {
    super(name);
    execute(commandContext -> {
      if (RequestManager.cancel()) {
        commandContext.sendSuccess("実行中のリクエストをキャンセルしました");
      } else {
        commandContext.sendFailure("実行中のリクエストはありません");
      }
    });
  }
}
