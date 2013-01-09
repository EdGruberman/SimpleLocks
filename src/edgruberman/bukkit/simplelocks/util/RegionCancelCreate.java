package edgruberman.bukkit.simplelocks.util;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.simplelocks.LockCreate;
import edgruberman.bukkit.simpleregions.Main;
import edgruberman.bukkit.simpleregions.Option;
import edgruberman.bukkit.simpleregions.Region;
import edgruberman.bukkit.simpleregions.commands.RegionExecutor;

public class RegionCancelCreate extends Option implements Listener {

    public RegionCancelCreate(final Plugin plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLockCreate(final LockCreate create) {
        for (final Region region : this.regions)
            if (region.contains(create.getBlock().getX(), create.getBlock().getY(), create.getBlock().getZ())) {
                this.plugin.getLogger().log(Level.FINEST, "Cancelling lock creation attempt by {0} in {1} at {2}", new Object[] { create.getCreator().getName(), region, create.getBlock() });
                Main.courier.send(create.getCreator(), "region-cancel-create", RegionExecutor.formatName(region));
                create.setCancelled(true);
                break;
            }
    }

}
