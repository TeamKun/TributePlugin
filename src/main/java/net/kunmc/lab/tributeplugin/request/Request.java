package net.kunmc.lab.tributeplugin.request;

import java.util.UUID;
import net.kunmc.lab.tributeplugin.Store;
import net.kunmc.lab.tributeplugin.util.message.MessageUtil;
import net.kunmc.lab.tributeplugin.util.text.TextColorPresets;
import net.kunmc.lab.tributeplugin.util.timer.DisplayType;
import net.kunmc.lab.tributeplugin.util.timer.Timer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class Request implements Listener {

  private final UUID requester;
  private final Timer timer;
  private final Material targetMaterial;
  private int targetAmount;

  public Request(Material targetMaterial, int targetAmount, int timeLimit) {
    this.requester = Store.config.requester.value();
    this.targetMaterial = targetMaterial;
    this.targetAmount = targetAmount;

    this.timer = new Timer(timeLimit);
    this.timer.setDisplayType(DisplayType.BOSSBAR)
        .setCountDown(5)
        .setEndProcess(new FailureEvent(this.requester))
        .start();
    Bukkit.getPluginManager().registerEvents(this, Store.plugin);

    Player player = Bukkit.getPlayer(this.requester);
    player.setGlowing(true);
    player.setInvulnerable(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityPickupItem(EntityPickupItemEvent event) {
    Player player;
    Item item;
    ItemStack itemStack;

    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    player = ((Player) event.getEntity()).getPlayer();

    if (!(player.getUniqueId().equals(this.requester))) {
      return;
    }

    item = event.getItem();
    itemStack = item.getItemStack();
    if (item.getThrower() == null || item.getThrower().equals(this.requester)) {
      return;
    }

    if (this.targetMaterial != itemStack.getType()) {
      return;
    }

    int amount = itemStack.getAmount();

    MessageUtil.broadcast(
        Component.text(Bukkit.getPlayer(item.getThrower()).getName())
            .append(Component.text("は"))
            .append(Component.text(Bukkit.getPlayer(this.requester).getName()))
            .append(Component.text("に"))
            .append(Component.translatable(this.targetMaterial.getTranslationKey()))
            .append(Component.text("を"))
            .append(Component.text(String.valueOf(amount)))
            .append(Component.text("個献上した。"))
            .color(TextColor.color(85, 255, 85)));

    this.targetAmount -= amount;

    // 成功
    if (this.targetAmount <= 0) {
      MessageUtil.broadcastTitle(TextColorPresets.GREEN.Prepend("成功!"), "", 20, 60, 20);
      this.timer.stop(false);
      Bukkit.getPlayer(this.requester).setGlowing(false);
    }
  }
}
