package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;

public class LockRevoke implements CommandExecutor {

    private final Locksmith locksmith;

    public LockRevoke(final Locksmith locksmith) {
        this.locksmith = locksmith;
    }

    // usage: /<command> <Name>
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requiresPlayer");
            return true;
        }

        if (args.length < 1) {
            Main.courier.send(sender, "requiresArgument", "<Name>");
            return false;
        }

        if (args[0].length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH) {
            Main.courier.send(sender, "nameTooLong");
            return false;
        }

        final Player player = (Player) sender;
        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.courier.send(sender, "requiresLock");
            return true;
        }

        if (!lock.isOwner(player)) {
            Main.courier.send(sender, "requiresOwner", label, lock.getOwner());
            return true;
        }

        if (!lock.hasExplicitAccess(args[0])) {
            Main.courier.send(sender, "revokeMissing", args[0]);
            return true;
        }

        lock.removeAccess(args[0]);
        Main.courier.send(sender, "revokeSuccess", args[0]);
        lock.refresh();
        return true;
    }

}
