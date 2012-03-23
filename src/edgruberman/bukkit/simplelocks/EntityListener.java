package edgruberman.bukkit.simplelocks;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

public final class EntityListener implements Listener {

    private final Plugin plugin;

    EntityListener(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        // Cancel event if any affected block is a lock or is locked.
        for (final Block block : event.blockList()) {
            if (Lock.isLock(block) || Lock.getLock(block) != null) {
                event.setCancelled(true);
                this.plugin.getLogger().log(Level.FINER,
                        "Cancelling explosion to protect lock at"
                            + " x:" + block.getX()
                            + " y:" + block.getY()
                            + " z:" + block.getZ()
                );
                break;
            }
        }
    }

}
