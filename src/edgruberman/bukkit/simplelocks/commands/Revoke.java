package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;

public class Revoke implements CommandExecutor {

    private final Locksmith locksmith;

    public Revoke(final Locksmith locksmith) {
        this.locksmith = locksmith;
    }

    // usage: /<command> <Name>
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player");
            return true;
        }

        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "name", 0);
            return false;
        }

        final Player player = (Player) sender;
        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.courier.send(sender, "requires-lock");
            return true;
        }

        if (!lock.hasAccess(player)) {
            Main.courier.send(sender, "requires-access", label, lock.getAccess());
            return true;
        }

        final String name = this.locksmith.getSubstitution(Bukkit.getOfflinePlayer(args[0]).getName());
        if (!lock.hasExplicitAccess(name)) {
            Main.courier.send(sender, "revoke.missing", name);
            return true;
        }

        lock.removeAccess(name);
        if (!lock.hasAccess(player)){
            Main.courier.send(sender, "revoke.prevent");
            lock.addAccess(name);
            return true;
        }

        Main.courier.send(sender, "revoke.success", name);
        return true;
    }

}
