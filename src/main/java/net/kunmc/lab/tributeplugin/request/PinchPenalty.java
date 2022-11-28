package net.kunmc.lab.tributeplugin.request;

import java.util.concurrent.ThreadLocalRandom;
import net.kunmc.lab.tributeplugin.Store;
import net.kunmc.lab.tributeplugin.util.calculation.ShapeUtils;
import net.kunmc.lab.tributeplugin.util.timer.RegularProcess;
import net.kunmc.lab.tributeplugin.util.timer.TimerContext;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PinchPenalty extends RegularProcess {

  @Override
  public boolean execute(TimerContext context) {
    new BukkitRunnable() {
      @Override
      public void run() {

        double progressRate = context.progressLate();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Location center = Store.config.requester.toPlayer().getEyeLocation();
        if (progressRate > 0.5) {
          return;
        }

        double particleProbability = 0.02;
        int vanishLimit = 2;
        if (progressRate < 0.2) {
          particleProbability = 0.1;
          vanishLimit = 5;
        }

        if (progressRate == 0) {
          particleProbability = 0.1;
        }

        double finalParticleProbability = particleProbability;
        ShapeUtils.getLocationSphereAround(center, Store.config.blackHaleRadius.value())
            .forEach(x -> {
              if (random.nextDouble() <= finalParticleProbability) {
                Store.queuedExecutor.offer(
                    new BukkitRunnable() {
                      @Override
                      public void run() {
                        spawnBlackParticle(x);
                        if (random.nextDouble() <= 0.2 && progressRate != 0) {
                          if (x.getBlock().getType() != Material.AIR) {
                            x.getBlock().setType(Material.AIR);
                          }
                        }
                      }
                    }
                );
              }
            });

        Bukkit.selectEntities(Bukkit.getConsoleSender(), "@e").stream()
            .filter(x -> x.getLocation().getWorld().equals(center.getWorld()))
            .filter(x -> x.getLocation().distance(center) <= Store.config.blackHaleRadius.value())
            .filter(x -> {
              if (x instanceof Player) {
                Player p = ((Player) x);
                return p.getGameMode() == GameMode.SURVIVAL
                    || p.getGameMode() == GameMode.ADVENTURE;
              }
              return true;
            })
            .forEach(x -> {
              if (x instanceof Player) {
                Store.queuedExecutor.offer(
                    new BukkitRunnable() {
                      @Override
                      public void run() {
                        ((Player) x).damage(1);
                      }
                    }
                );
              }
            });
      }
    }.runTaskAsynchronously(Store.plugin);
    return false;
  }

  private void spawnBlackParticle(Location loc) {
    loc.getWorld()
        .spawnParticle(Particle.REDSTONE, loc, 3,
            new Particle.DustOptions(Color.BLACK, 10));
  }

}
