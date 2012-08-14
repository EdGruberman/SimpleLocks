package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;

public class LockOwner implements CommandExecutor {

    private final Locksmith locksmith;

    public LockOwner(final Locksmith locksmith) {
        this.locksmith = locksmith;
    }

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

        lock.setOwner(args[0]);
        Main.courier.send(sender, "ownerSuccess", args[0]);
        lock.refresh();
        return true;
    }

}
