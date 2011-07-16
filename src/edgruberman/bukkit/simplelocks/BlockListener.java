package edgruberman.bukkit.simplelocks;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;

public final class BlockListener extends org.bukkit.event.block.BlockListener {
    
    BlockListener(Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Normal, plugin);
        pluginManager.registerEvent(Event.Type.SIGN_CHANGE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Lock lock = Lock.getLock(event.getBlock());
        if (lock == null) return;
        
        // Allow lock owner to break lock.
        if (lock.isOwner(event.getPlayer().getName())) return;
        
        event.setCancelled(true);
        Main.messageManager.send(event.getPlayer(), "You can not remove a lock unless you are the owner.", MessageLevel.RIGHTS, false);
        Main.messageManager.log(
                "Cancelled block break to protect lock at"
                    + " x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
                , MessageLevel.FINER
        );
    }
    
    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) return;

        // Do not turn into lock unless trigger is present.
        if (!Main.hasTrigger(event.getLine(0))) return;
        
        // Do not modify existing locks further.
        if (Lock.isLock(event.getBlock())) return;
        
        // Check for default owner substitute. (Useful for long names that won't fit on a sign.)
        String ownerName = Main.getDefaultOwner(event.getPlayer());
        if (ownerName.length() > 15) {
            Main.messageManager.send(event.getPlayer(), "Unable to create lock; Owner name is too long.", MessageLevel.SEVERE, false);
            return;
        }

        // Create new lock.
        Lock lock = new Lock(event.getBlock(), ownerName, event.getLines());
        if (!lock.isLock()) return;
        
        // Set event to use newly configured lock lines.
        for(int i = 0; i < lock.getSignBlock().getLines().length; i++) {
            event.setLine(i, lock.getSignBlock().getLine(i));
        }
    }
}