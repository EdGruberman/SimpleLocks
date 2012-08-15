package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;

public class LockDescribe implements CommandExecutor {

    private final Locksmith locksmith;

    public LockDescribe(final Locksmith locksmith) {
        this.locksmith = locksmith;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requiresPlayer");
            return true;
        }

        final Player player = (Player) sender;
        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.courier.send(sender, "requiresLock");
            return true;
        }

        Main.courier.send(player, "describe"
                , lock.getOwner(), lock.getAccess().toString().replaceAll("^\\[|\\]$", "")
                , lock.isOwner(player)?1:0, lock.hasAccess(player)?1:0
                , lock.getAccess().size());
        return true;
    }

}
