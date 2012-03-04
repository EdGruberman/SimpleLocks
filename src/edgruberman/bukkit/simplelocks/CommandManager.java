package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class CommandManager implements CommandExecutor {
    private final Main plugin;

    protected CommandManager (final Main plugin) {
        this.plugin = plugin;

        this.setExecutorOf("lock");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] split) {
        Main.messageManager.log(
                ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                    + " issued command: " + label + " " + CommandManager.join(split)
                , MessageLevel.FINE
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

        final Lock lock = Lock.getLock(player.getTargetBlock((HashSet<Byte>) null, 3));
        if (lock == null || !lock.isLock()) {
            Main.messageManager.respond(sender, "No lock identifed.", MessageLevel.WARNING, false);
            return true;
        }

        if (action.equals("info")) {
            this.actionInfo(player, lock);
            return true;
        }

        if (!sender.isOp() && !lock.isOwner(player)) {
            Main.messageManager.respond(sender, "You must be the lock owner to use that command.", MessageLevel.RIGHTS, false);
            return true;
        }

        if (action.equals("+access")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.respond(sender, "No name specified to add access to for this lock.", MessageLevel.WARNING, false);
                return true;
            }

            if (lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, "\"" + split[1] + "\" already has direct access to this lock.", MessageLevel.WARNING, false);
                return true;
            }

            lock.addAccess(split[1]);
            if (lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, "\"" + split[1] + "\" now has direct access to this lock.", MessageLevel.STATUS, false);
                lock.refresh();
            } else {
                Main.messageManager.respond(sender, "Unable to add direct access to \"" + split[1] + "\" for this lock.", MessageLevel.SEVERE, false);
            }

            return true;
        }

        if (action.equals("-access")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.respond(sender, "No name specified to remove access from for this lock.", MessageLevel.WARNING, false);
                return true;
            }

            if (!lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, "\"" + split[1] + "\" does not currently have direct access to this lock.", MessageLevel.WARNING, false);
                return true;
            }

            lock.removeAccess(split[1]);
            if (!lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, "\"" + split[1] + "\" had direct access removed for this lock.", MessageLevel.STATUS, false);
                lock.refresh();
            } else {
                Main.messageManager.respond(sender, "Unable to remove direct access for \"" + split[1] + "\" on this lock.", MessageLevel.SEVERE, false);
            }

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
                Main.messageManager.respond(sender, "No name specified to change lock owner to.", MessageLevel.WARNING, false);
                return true;
            }

            lock.removeAccess(split[1]);
            lock.setOwner(split[1]);
            if (lock.isOwner(split[1], true)) {
                Main.messageManager.respond(player, "\"" + split[1] + "\" has been set as the owner for this lock.", MessageLevel.STATUS, false);
                lock.refresh();
            } else {
                Main.messageManager.respond(sender, "Unable to set \"" + split[1] + "\" as owner for this lock.", MessageLevel.SEVERE, false);
            }

            return true;
        }

        if (action.equals("break")) {
            Main.messageManager.log(
                    "Lock broken by " + player.getName() + " at "
                        + " x:" + lock.getBlock().getX()
                        + " y:" + lock.getBlock().getY()
                        + " z:" + lock.getBlock().getZ()
                    , MessageLevel.FINE
            );
            lock.getBlock().setType(Material.AIR);
            lock.getBlock().getWorld().dropItemNaturally(lock.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
            Main.messageManager.respond(sender, "Lock broken.", MessageLevel.STATUS, false);
            return true;
        }

        if (action.equals("reload")) {
            this.plugin.loadConfiguration();
            Main.messageManager.respond(sender, "Configuration reloaded.", MessageLevel.STATUS);
            return true;
        }


        return true;
    }

    private void actionInfo(final Player player, final Lock lock) {
        if (!lock.isLock()) {
            Main.messageManager.send(player, "No lock identifed at target.", MessageLevel.WARNING, false);
            return;
        }

        Main.messageManager.send(player, lock.getDescription(), MessageLevel.CONFIG, false);

        if (!lock.hasAccess(player)) {
            Main.messageManager.send(player, "You do not have access to this lock.", MessageLevel.RIGHTS, false);
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
            Main.messageManager.log("Unable to register \"" + label + "\" command.", MessageLevel.WARNING);
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