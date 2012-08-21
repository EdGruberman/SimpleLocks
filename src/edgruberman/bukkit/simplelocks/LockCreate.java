package edgruberman.bukkit.simplelocks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LockCreate extends Event implements Cancellable {

    private boolean cancel = false;
    private final Block block;
    private final BlockFace attached;
    private final String owner;
    private final CommandSender creator;

    LockCreate(final Block block, final BlockFace attached, final String owner, final CommandSender creator) {
        this.block = block;
        this.attached = attached;
        this.owner = owner;
        this.creator = creator;
    }

    public Block getBlock() {
        return this.block;
    }

    public BlockFace getAttached() {
        return this.attached;
    }

    public String getOwner() {
        return this.owner;
    }

    public CommandSender getCreator() {
        return this.creator;
    }

    // --- Cancellable Event ----

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    // ---- Custom Event Handlers ----

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return LockCreate.handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return LockCreate.handlers;
    }

}
