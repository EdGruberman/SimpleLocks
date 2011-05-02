package edgruberman.bukkit.simplelocks;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;

import edgruberman.bukkit.simplelocks.MessageManager.MessageLevel;

public class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    public EntityListener() {}
    
    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        
        // Cancel event if any affected block is a lock or is locked.
        for (Block block : event.blockList()) {
            if (Lock.isLock(block) || Lock.getLock(block) != null) {
                event.setCancelled(true);
                Main.messageManager.log(MessageLevel.FINER
                        , "Cancelling explosion to protect lock at"
                        + " x:" + block.getX()
                        + " y:" + block.getY()
                        + " z:" + block.getZ()
                );
                break;
            }
        }
    }
}
