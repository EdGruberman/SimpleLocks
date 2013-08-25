package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.Main;
import edgruberman.bukkit.simplelocks.util.Feedback;

public class Break implements CommandExecutor {

    private final Locksmith locksmith;

    public Break(final Locksmith locksmith) {
         this.locksmith = locksmith;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Main.courier.send(sender, "requires-player");
            return true;
        }

        final Player player = (Player) sender;
        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.courier.send(sender, "requires-lock");
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        lock.sign.setType(Material.AIR);
        lock.sign.update(true);
        lock.sign.getWorld().dropItemNaturally(lock.sign.getLocation(), new ItemStack(Material.SIGN, 1));
        Main.courier.send(sender, "break");
        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
        return true;
    }

}
