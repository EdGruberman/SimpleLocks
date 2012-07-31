package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;

public class LockInfo implements CommandExecutor {

    private final Locksmith locksmith;

    public LockInfo(final Locksmith locksmith) {
        this.locksmith = locksmith;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.messenger.tell(sender, "requiresPlayer");
            return true;
        }

        final Player player = (Player) sender;
        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.messenger.tell(sender, "requiresLock");
            return true;
        }

        final Block locked = lock.getLocked();
        Main.messenger.tell(sender, "info", lock.getOwner(), lock.getAccess().toString().replaceAll("^\\[|\\]$", ""), locked.getType().toString(), locked.getX(), locked.getY(), locked.getZ());
        Main.messenger.tell(sender, (!lock.hasAccess(player) ? "denied" : (lock.isOwner(player) ? "owner" : "access")));
        return true;
    }

}
