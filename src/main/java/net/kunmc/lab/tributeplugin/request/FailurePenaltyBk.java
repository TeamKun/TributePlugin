package net.kunmc.lab.tributeplugin.request;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.kunmc.lab.tributeplugin.Store;
import net.kunmc.lab.tributeplugin.util.message.MessageUtil;
import net.kunmc.lab.tributeplugin.util.timer.EndProcess;
import net.kunmc.lab.tributeplugin.util.timer.TimerContext;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FailurePenaltyBk extends EndProcess {

  private UUID requester;

  public FailurePenaltyBk(UUID requester) {
    this.requester = requester;
  }

  @Override
  public void execute(TimerContext context) {
    MessageUtil.broadcast("失敗");
    Player player = Bukkit.getPlayer(requester);
//    new BukkitRunnable() {
//      @Override
//      public void run() {
//        player.getWorld().createExplosion(player.getLocation(), 10);
//      }
//    }.runTask(Store.plugin);
    onExplosionPrime();
    player.setGlowing(false);
  }

  private void onExplosionPrime() {
    Player player = Bukkit.getPlayer(requester);
    Location center = player.getLocation();
    World world = center.getWorld();
    int totalTick = 120;

    new BukkitRunnable() {
      private int tick = 0;

      @Override
      public void run() {
        world.spawnParticle(Particle.REDSTONE, center, 3,
            new Particle.DustOptions(Color.BLACK, 10));
        tick += 4;
        if (tick >= totalTick) {
          cancel();
        }
      }
    }.runTaskTimerAsynchronously(Store.plugin, 0, 4);

    Set<Entity> involvedEntities = Collections.synchronizedSet(new HashSet<>());
    center.getBlock().setType(Material.AIR);
    new BukkitRunnable() {
      private int tick = 0;
      private final ThreadLocalRandom random = ThreadLocalRandom.current();

      @Override
      public void run() {
        int radius = Store.config.blackHaleRadius.value();
        sphereAround(center, radius).forEach(x -> {
          if (x.isEmpty()) {
            return;
          }

          if (random.nextDouble() <= 0.1) {
            FallingBlock fallingBlock = world.spawnFallingBlock(x.getLocation(), x.getBlockData());
            fallingBlock.setGravity(false);
            fallingBlock.setInvulnerable(true);
            involvedEntities.add(fallingBlock);
            x.setType(Material.AIR);
          }
        });

        AtomicInteger n = new AtomicInteger();
        long reduceQuantity =
            involvedEntities.stream().filter(x -> x instanceof FallingBlock).count()
                - 150;
        synchronized (involvedEntities) {
          involvedEntities.stream()
              .filter(x -> x instanceof FallingBlock)
              .filter(x -> n.getAndAdd(1) < reduceQuantity)
              .peek(Entity::remove)
              .collect(Collectors.toList())
              .forEach(involvedEntities::remove);
        }

        Bukkit.selectEntities(Bukkit.getConsoleSender(), "@e").stream()
            .filter(x -> x.getLocation().getWorld().equals(center.getWorld()))
            .filter(x -> x.getLocation().distance(center) <= radius)
            .filter(x -> {
              if (x instanceof Player) {
                Player p = ((Player) x);
                return p.getGameMode() == GameMode.SURVIVAL
                    || p.getGameMode() == GameMode.ADVENTURE;
              }
              return true;
            })
            .forEach(involvedEntities::add);

        tick += 4;
        if (tick >= totalTick) {
          cancel();
        }
      }
    }.runTaskTimer(Store.plugin, 0, 4);

    new BukkitRunnable() {
      private int tick = 0;

      @Override
      public void run() {
        involvedEntities.forEach(x -> {
          Vector sub = center.toVector().subtract(x.getLocation().toVector());
          sub.multiply(0.35 / sub.length());
          x.setVelocity(sub);
          if (x instanceof LivingEntity) {
            ((LivingEntity) x).damage(0.3);
          }

          if (x instanceof FallingBlock) {
            if (x.getLocation().toVector().distance(center.toVector()) < 1) {
              x.remove();
            }
          }
        });

        tick++;
        if (tick >= totalTick) {
          involvedEntities.stream()
              .filter(x -> x instanceof FallingBlock)
              .forEach(Entity::remove);
          cancel();
        }
      }
    }.runTaskTimer(Store.plugin, 0, 0);
  }

  private Set<Block> sphereAround(Location location, int radius) {
    Set<Block> sphere = new HashSet<>();
    Block center = location.getBlock();
    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          Block b = center.getRelative(x, y, z);
          if (center.getLocation().distance(b.getLocation()) <= radius) {
            sphere.add(b);
          }
        }
      }
    }
    return sphere;
  }
}
