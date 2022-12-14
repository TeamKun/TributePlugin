package net.kunmc.lab.tributeplugin.request;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.kunmc.lab.configlib.value.UUIDValue;
import net.kunmc.lab.tributeplugin.Store;
import net.kunmc.lab.tributeplugin.util.acitonbar.ActionBarManager;
import net.kunmc.lab.tributeplugin.util.message.MessageUtil;
import net.kunmc.lab.tributeplugin.util.sound.SoundUtils;
import net.kunmc.lab.tributeplugin.util.text.TextColorPresets;
import net.kunmc.lab.tributeplugin.util.timer.DisplayType;
import net.kunmc.lab.tributeplugin.util.timer.Timer;
import net.kunmc.lab.tributeplugin.util.timer.TimerStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class Request implements Listener {

  private final UUIDValue requester;
  private final Timer timer;
  private final Material targetMaterial;
  private int targetAmount;
  private final Map<String, Integer> presentationCount = new HashMap<>();
  private String actionBarKey;

  public Request(Material targetMaterial, int targetAmount, int timeLimit) {
    this.requester = Store.config.requester;
    this.targetMaterial = targetMaterial;
    this.targetAmount = targetAmount;
    this.actionBarKey = UUID.randomUUID().toString();
    this.timer = new Timer(timeLimit);
    this.timer.setDisplayType(DisplayType.BOSSBAR)
        .setCountDown(5, true)
        .setRegularProcess(new PinchPenalty())
        .setEndProcess(new FailurePenalty(this.requester, actionBarKey))
        .start();
    Bukkit.getPluginManager().registerEvents(this, Store.plugin);

    Player player = requester.toPlayer();
    player.setGlowing(true);
    player.setInvulnerable(true);

    MessageUtil.broadcast(
        Component.text("\n!!!??????????????????!!!\n")
            .append(Component.text(this.requester.playerName()))
            .append(Component.text("???"))
            .append(Component.translatable(this.targetMaterial.getTranslationKey()))
            .append(Component.text("???"))
            .append(Component.text(this.targetAmount))
            .append(Component.text("?????????????????????!\n"))
            .color(TextColorPresets.YELLOW.component())
    );

    ActionBarManager.create(actionBarKey, buildActionBarText());
  }

  void cancel() {
    EntityPickupItemEvent.getHandlerList().unregister(this);
    PlayerDropItemEvent.getHandlerList().unregister(this);
    this.timer.stop(false);
    ActionBarManager.stop(this.actionBarKey);
    this.requester.toPlayer().setGlowing(false);
  }

  TimerStatus status() {
    return this.timer.status();
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

    if (!(player.getUniqueId().equals(this.requester.value()))) {
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
    int count = amount;
    UUID throwerId = item.getThrower();
    String throwerName = Bukkit.getPlayer(throwerId).getName();

    if (throwerId.equals(this.requester.value())) {
      return;
    }

    if (this.presentationCount.containsKey(throwerName)) {
      count += this.presentationCount.get(throwerName);
    }
    this.presentationCount.put(throwerName, count);

    MessageUtil.broadcast(
        Component.text(throwerName)
            .append(Component.text("???"))
            .append(Component.text(this.requester.toPlayer().getName()))
            .append(Component.text("???"))
            .append(Component.translatable(this.targetMaterial.getTranslationKey()))
            .append(Component.text("???"))
            .append(Component.text(String.valueOf(amount)))
            .append(Component.text("??????????????????"))
            .color(TextColor.color(85, 255, 85)));

    this.targetAmount -= amount;
    if (this.targetAmount < 0) {
      this.targetAmount = 0;
    }
    ActionBarManager.setText(this.actionBarKey, buildActionBarText());

    // ??????
    if (this.targetAmount <= 0) {
      MessageUtil.broadcastTitle(TextColorPresets.GREEN.Prepend("??????!"), "", 20, 60, 20);
      this.requester.toPlayer().setGlowing(false);
      cancel();
      MessageUtil.broadcastTitle("??????!", "", 20, 60, 20);
      SoundUtils.broadcastSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
      Component resultMessage = Component.text("\n??????????????????!\n")
          .append(Component.text("??????\n"));
      AtomicReference<Component> finalResultMessage = new AtomicReference<>(resultMessage);
      this.presentationCount.forEach((playerName, presentAmount) -> {
        finalResultMessage.set(finalResultMessage.get().append(Component.text(playerName))
            .append(Component.text(": "))
            .append(Component.text(String.valueOf(presentAmount)))
            .append(Component.text("???\n")));
      });
      Component msg = finalResultMessage.get().color(TextColorPresets.GREEN.component());
      MessageUtil.broadcast(msg);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (!event.getPlayer().getUniqueId().equals(this.requester.value())) {
      return;
    }

    if (event.getItemDrop().getItemStack().getType() != this.targetMaterial) {
      return;
    }

    event.setCancelled(true);
  }

  private Component buildActionBarText() {
    return Component.translatable(this.targetMaterial.getTranslationKey())
        .append(Component.text(" ??????"))
        .append(Component.text(this.targetAmount))
        .append(Component.text("???"));
  }
}
