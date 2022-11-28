package net.kunmc.lab.tributeplugin.request;

import java.util.Objects;
import net.kunmc.lab.tributeplugin.util.timer.TimerStatus;
import org.bukkit.Material;

public class RequestManager {

  private static Request request;

  public static void start(Material item, int amount, int timeLimit) {
    if (Objects.nonNull(request)) {
      request.cancel();
    }
    request = new Request(item, amount, timeLimit);
  }

  public static boolean cancel() {
    if (Objects.isNull(request)) {
      return false;
    }

    if (request.status() != TimerStatus.Running) {
      return false;
    }
    
    request.cancel();
    return true;
  }
}
