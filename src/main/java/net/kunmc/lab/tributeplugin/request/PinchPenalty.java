package net.kunmc.lab.tributeplugin.request;

import net.kunmc.lab.tributeplugin.util.timer.RegularProcess;
import net.kunmc.lab.tributeplugin.util.timer.TimerContext;

public class PinchPenalty extends RegularProcess {

  private boolean isRunning;

  @Override
  public boolean execute(TimerContext context) {

    double progressRate = context.progressLate();

    if (progressRate > 0.5) {
      return false;
    }

    if (this.isRunning) {
      return false;
    }

    this.isRunning = true;
    
    return false;
  }
}
