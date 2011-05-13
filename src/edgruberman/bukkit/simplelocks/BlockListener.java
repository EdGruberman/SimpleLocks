package edgruberman.bukkit.simplelocks;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class BlockListener extends org.bukkit.event.block.BlockListener {
    
    public BlockListener() {}
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Lock lock = Lock.getLock(event.getBlock());
        if (lock == null) return;
        
        // Allow lock owner to break lock.
        if (lock.isOwner(event.getPlayer().getName())) return;
        
        event.setCancelled(true);
        Main.messageManager.send(event.getPlayer(), MessageLevel.RIGHTS
                , "You can not remove a lock unless you are the owner.");
        Main.messageManager.log(MessageLevel.FINER
                , "Cancelled block break to protect lock at"
                + " x:" + event.getBlock().getX()
                + " y:" + event.getBlock().getY()
                + " z:" + event.getBlock().getZ()
        );
    }
    
    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) return;

        // Do not turn into lock unless trigger is present.
        if (!Main.hasTrigger(event.getLine(0))) return;
        
        // Do not modify existing locks further.
        if (Lock.isLock(event.getBlock())) return;

        // Create new lock.
        Lock lock = new Lock(event.getBlock(), event.getPlayer(), event.getLines());
        if (!lock.isLock()) return;
        
        // Set event to use newly configured lock lines.
        for(int i = 0; i < lock.getSignBlock().getLines().length; i++) {
            event.setLine(i, lock.getSignBlock().getLine(i));
        }
    }
}