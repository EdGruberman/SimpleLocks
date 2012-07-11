package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class CommandManager implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Locksmith locksmith;

    protected CommandManager (final JavaPlugin plugin, final Locksmith locksmith) {
        this.plugin = plugin;
        this.locksmith = locksmith;
        this.setExecutorOf("simplelocks:lock");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        this.plugin.getLogger().fine(
                ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                    + " issued command: " + label + " " + CommandManager.join(split)
        );

        if (!(sender instanceof Player)) return false;

        final Player player = (Player) sender;

        String action;
        if (split == null || split.length == 0) {
            action = "info";
        } else {
            action = split[0].toLowerCase();
            if (action.equals("+"))   action = "+access";
            if (action.equals("add")) action = "+access";
            if (action.equals("-"))      action = "-access";
            if (action.equals("remove")) action = "-access";
        }

        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            Main.messageManager.tell(sender, "No lock identifed", MessageLevel.WARNING, false);
            return true;
        }

        if (action.equals("info")) {
            this.actionInfo(player, lock);
            return true;
        }

        if (!sender.isOp() && !lock.isOwner(player)) {
            Main.messageManager.tell(sender, "You must be the lock owner to use that command", MessageLevel.RIGHTS, false);
            return true;
        }

        if (action.equals("+access")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.tell(sender, "No name specified to add access to for this lock", MessageLevel.WARNING, false);
                return true;
            }

            if (lock.hasExplicitAccess(split[1])) {
                Main.messageManager.tell(sender, "\"" + split[1] + "\" already has explicit access to this lock", MessageLevel.WARNING, false);
                return true;
            }

            if (lock.getAccess().size() == 2) {
                Main.messageManager.tell(sender, "Unable to add lock access to \"" + split[1] + "\"; Access full", MessageLevel.WARNING, false);
                return true;
            }

            lock.addAccess(split[1]);
            Main.messageManager.tell(sender, "\"" + split[1] + "\" now has explicit access to this lock", MessageLevel.STATUS, false);
            lock.refresh();
            return true;
        }

        if (action.equals("-access")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.tell(sender, "No name specified to remove access from for this lock", MessageLevel.WARNING, false);
                return true;
            }

            if (!lock.hasExplicitAccess(split[1])) {
                Main.messageManager.tell(sender, "\"" + split[1] + "\" does not currently have explicit access to this lock", MessageLevel.WARNING, false);
                return true;
            }

            lock.removeAccess(split[1]);
            Main.messageManager.tell(sender, "\"" + split[1] + "\" had explicit access removed for this lock", MessageLevel.STATUS, false);
            lock.refresh();
            return true;
        }


        // ---- Only server operators can use commands past this point.
        if (!sender.isOp())
            return false;

        if (action.equals("pick")) {
            // TODO make this work
            return true;
        }

        if (action.equals("owner")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.tell(sender, "No name specified to change lock owner to", MessageLevel.WARNING, false);
                return true;
            }

            if (lock.hasExplicitAccess(split[1])) lock.removeAccess(split[1]);
            lock.setOwner(split[1]);
            Main.messageManager.tell(player, "\"" + split[1] + "\" has been set as the owner for this lock", MessageLevel.STATUS, false);
            lock.refresh();
            return true;
        }

        if (action.equals("break")) {
            this.plugin.getLogger().fine(
                    "Lock broken by " + player.getName() + " at "
                        + " x:" + lock.sign.getX()
                        + " y:" + lock.sign.getY()
                        + " z:" + lock.sign.getZ()
            );
            lock.sign.setType(Material.AIR);
            lock.update();
            lock.sign.getWorld().dropItemNaturally(lock.sign.getLocation(), new ItemStack(Material.SIGN, 1));
            Main.messageManager.tell(sender, "Lock broken", MessageLevel.STATUS, false);
            return true;
        }

        if (action.equals("reload")) {
            ((Main) this.plugin).start(this.plugin, this.plugin.getConfig());
            Main.messageManager.tell(sender, "Configuration reloaded", MessageLevel.STATUS);
            return true;
        }


        return true;
    }

    private void actionInfo(final Player player, final Lock lock) {
        Main.messageManager.send(player, "Lock = " + lock.getDescription(), MessageLevel.CONFIG, false);

        if (!lock.hasAccess(player)) {
            Main.messageManager.send(player, "You do not have access to this lock", MessageLevel.RIGHTS, false);
        } else if (lock.isOwner(player)) {
            Main.messageManager.send(player, "To modify: /lock (+|-) <Player>", MessageLevel.NOTICE, false);
        }
    }

    /**
     * Registers this class as executor for a chat/console command.
     *
     * @param label Command label to register.
     */
    private void setExecutorOf(final String label) {
        final PluginCommand command = this.plugin.getCommand(label);
        if (command == null) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to register \"" + label + "\" command");
            return;
        }

        command.setExecutor(this);
    }

    /**
     * Concatenate all string elements of an array together with a space.
     *
     * @param s String array
     * @return Concatenated elements
     */
    private static String join(final String[] s) {
        return CommandManager.join(Arrays.asList(s), " ");
    }

    /**
     * Combine all the elements of a list together with a delimiter between each.
     *
     * @param list List of elements to join.
     * @param delim Delimiter to place between each element.
     * @return String combined with all elements and delimiters.
     */
    private static String join(final List<String> list, final String delim) {
        if (list == null || list.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder();
        for (final String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());

        return sb.toString();
    }

}
