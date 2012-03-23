package edgruberman.bukkit.simplelocks;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

public final class BlockListener implements Listener {

    private final Plugin plugin;

    BlockListener(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) return;

        final Lock lock = Lock.getLock(event.getBlock());
        if (lock == null) return;

        // Allow lock owner to break lock.
        if (lock.isOwner(event.getPlayer().getName())) return;

        event.setCancelled(true);
        Main.messageManager.send(event.getPlayer(), "You can not remove a lock unless you are the owner", MessageLevel.RIGHTS, false);
        this.plugin.getLogger().log(Level.FINER,
                "Cancelled block break to protect lock at"
                    + " x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
        );

        lock.refresh();
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        if (event.isCancelled()) return;

        // Do not turn into lock unless trigger is present.
        if (!Main.hasTrigger(event.getLine(0))) return;

        // Do not modify existing locks further.
        if (Lock.isLock(event.getBlock())) return;

        // Check for default owner substitute. (Useful for long names that won't fit on a sign.)
        final String ownerName = Main.getDefaultOwner(event.getPlayer());
        if (ownerName.length() > 15) {
            Main.messageManager.send(event.getPlayer(), "Unable to create lock; Owner name is too long", MessageLevel.SEVERE, false);
            return;
        }

        // Create new lock.
        final Lock lock = new Lock(event.getBlock(), ownerName, event.getLines());
        if (!lock.isLock()) return;

        // Set event to use newly configured lock lines.
        for(int i = 0; i < lock.getState().getLines().length; i++) {
            event.setLine(i, lock.getState().getLine(i));
        }
    }

}
