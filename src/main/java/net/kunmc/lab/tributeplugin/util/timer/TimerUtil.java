package net.kunmc.lab.tributeplugin.util.timer;

import java.util.Objects;

class TimerUtil {

  static String limitText(String displayName, double currentTime) {
    String text = "";
    if (Objects.nonNull(displayName)) {
      text = displayName;
    }
    return text
        .concat(" 残り ")
        .concat(String.valueOf((int) Math.floor(currentTime)))
        .concat("秒");
  }

  static double progressRate(double currentTime, double limit) {
    double rate = currentTime / limit;
    if (rate < 0) {
      rate = 0;
    }
    return rate;
  }
}
