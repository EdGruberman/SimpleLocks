package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Aliaser;
import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;
import edgruberman.bukkit.simplelocks.util.Feedback;

public class Grant implements CommandExecutor {

    private final Locksmith locksmith;
    private final Aliaser aliaser;

    public Grant(final Locksmith locksmith, final Aliaser aliaser) {
        this.locksmith = locksmith;
        this.aliaser = aliaser;
    }

    // usage: /<command> name
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player");
            return true;
        }

        final Player player = (Player) sender;

        if (args.length < 1) {
            Main.courier.send(sender, "requires-argument", "name", 0);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return false;
        }

        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.courier.send(sender, "requires-lock");
            Feedback.COMMAND_RESULT_WARNING.send(player);
            return true;
        }

        if (!lock.hasAccess(player)) {
            Main.courier.send(sender, "requires-access", label);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        final String name = this.aliaser.getAlias(Bukkit.getOfflinePlayer(args[0]).getName());
        if (name.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH) {
            Main.courier.send(sender, "requires-alias", name, name.length(), Locksmith.MAXIMUM_SIGN_LINE_LENGTH);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        if (lock.hasExplicitAccess(name)) {
            Main.courier.send(sender, "grant.already", name);
            Feedback.COMMAND_RESULT_WARNING.send(player);
            return true;
        }

        if (lock.getAccess().size() == 3) {
            Main.courier.send(sender, "grant.full", name);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        lock.addAccess(name);
        Main.courier.send(sender, "grant.success", name);
        Feedback.COMMAND_RESULT_SUCCESS.send(player);
        return true;
    }

}
