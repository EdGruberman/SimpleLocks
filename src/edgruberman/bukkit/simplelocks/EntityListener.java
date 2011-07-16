package edgruberman.bukkit.simplelocks;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

public final class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    EntityListener(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_EXPLODE, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        
        // Cancel event if any affected block is a lock or is locked.
        for (Block block : event.blockList()) {
            if (Lock.isLock(block) || Lock.getLock(block) != null) {
                event.setCancelled(true);
                Main.messageManager.log(
                        "Cancelling explosion to protect lock at"
                            + " x:" + block.getX()
                            + " y:" + block.getY()
                            + " z:" + block.getZ()
                        , MessageLevel.FINER
                );
                break;
            }
        }
    }
}
