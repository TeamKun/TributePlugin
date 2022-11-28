package net.kunmc.lab.tributeplugin.request;

import static net.kunmc.lab.tributeplugin.util.calculation.ShapeUtils.getBlockSphereAround;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.kunmc.lab.configlib.value.UUIDValue;
import net.kunmc.lab.tributeplugin.Store;
import net.kunmc.lab.tributeplugin.util.timer.EndProcess;
import net.kunmc.lab.tributeplugin.util.timer.TimerContext;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FailurePenalty extends EndProcess {

  private UUIDValue requester;

  public FailurePenalty(UUIDValue requester) {
    this.requester = requester;
  }

  @Override
  public void execute(TimerContext context) {
    onExplosionPrime();
    requester.toPlayer().setGlowing(false);
  }

  private void onExplosionPrime() {
    Location center = this.requester.toPlayer().getLocation();
    World world = center.getWorld();
    int totalTick = 60;
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Set<Entity> involvedEntities = Collections.synchronizedSet(new HashSet<>());
    new BukkitRunnable() {
      @Override
      public void run() {
        int radius = Store.config.blackHaleRadius.value();
        Set<Block> blockSet = new HashSet<>();

        for (int i = 0; i < radius; i++) {
          getBlockSphereAround(center, i).forEach(block -> {
            blockSet.add(block);
          });
        }

        blockSet.forEach(x -> {
          Store.queuedExecutor.offer(
              new BukkitRunnable() {
                @Override
                public void run() {
                  if (x.isEmpty()) {
                    return;
                  }

                  if (random.nextDouble() <= 0.05) {
                    FallingBlock fallingBlock = world.spawnFallingBlock(x.getLocation(),
                        x.getBlockData());
                    fallingBlock.setGravity(false);
                    fallingBlock.setInvulnerable(true);
                    involvedEntities.add(fallingBlock);
                  }
                  x.setType(Material.AIR);
                }
              }
          );
        });
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
      }
    }.runTaskAsynchronously(Store.plugin);

    new BukkitRunnable() {
      private int tick = 0;

      @Override
      public void run() {
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1, 1);
        involvedEntities.forEach(x -> {
          Vector sub = center.toVector().subtract(x.getLocation().toVector());
          sub.multiply(0.35 / sub.length());
          x.setVelocity(sub);
          Store.queuedExecutor.offer(
              new BukkitRunnable() {
                @Override
                public void run() {
                  if (x.getLocation().toVector().distance(center.toVector()) < 1) {
                    if (x instanceof FallingBlock) {
                      x.remove();
                    }
                    if (x instanceof LivingEntity) {
                      ((LivingEntity) x).damage(1000);
                    }
                  }
                  center.getWorld().spawnParticle(Particle.REDSTONE, x.getLocation(), 3,
                      new Particle.DustOptions(Color.BLACK, 5));
                }
              }
          );
        });

        tick++;
        if (tick >= totalTick) {
          involvedEntities.stream()
              .filter(x -> x instanceof FallingBlock)
              .forEach(Entity::remove);
          cancel();
        }
      }
    }.runTaskTimerAsynchronously(Store.plugin, 0, 1);
  }
}
