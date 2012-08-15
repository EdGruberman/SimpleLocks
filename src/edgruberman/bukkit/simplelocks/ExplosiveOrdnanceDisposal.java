package edgruberman.bukkit.simplelocks;

import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

/** protect locks and locked blocks from explosions */
class ExplosiveOrdnanceDisposal implements Listener {

    private final Plugin plugin;
    private final Locksmith locksmith;

    ExplosiveOrdnanceDisposal(final Plugin plugin, final Locksmith locksmith) {
        this.plugin = plugin;
        this.locksmith = locksmith;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent explosion) {
        final Iterator<Block> affected = explosion.blockList().iterator();
        while (affected.hasNext()) {
            final Block block = affected.next();
            if (!this.locksmith.isLock(block) && !this.locksmith.isLocked(block)) continue;

            affected.remove();
            this.plugin.getLogger().log(Level.FINEST, "Protected lock from explosion at {0}", block);
        }
    }

}
