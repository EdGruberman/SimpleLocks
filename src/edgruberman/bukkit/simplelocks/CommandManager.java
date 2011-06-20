package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
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
    private Main plugin;

    protected CommandManager (Main plugin) {
        this.plugin = plugin;
        
        this.setExecutorOf("lock");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        Main.messageManager.log(MessageLevel.FINE
                , ((sender instanceof Player) ? ((Player) sender).getName() : "[CONSOLE]")
                + " issued command: " + label + " " + join(split)
        );
        
        if (!(sender instanceof Player)) return false;
        
        Player player = (Player) sender;
        
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
        
        Lock lock = Lock.getLock(player.getTargetBlock(null, 3));
        if (lock == null || !lock.isLock()) {
            Main.messageManager.respond(sender, MessageLevel.WARNING
                    , "No lock identifed.");
            return true;
        }

        if (action.equals("info")) {
            this.actionInfo(player, lock);
            return true;
        }
        
        if (!sender.isOp() && !lock.isOwner(player)) {
            Main.messageManager.respond(sender, MessageLevel.RIGHTS
                    , "You must be the lock owner to use that command.");
            return true;
        }
        
        if (action.equals("+access")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.respond(sender, MessageLevel.WARNING
                        , "No name specified to add access to for this lock.");
                return true;
            }
            
            if (lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, MessageLevel.WARNING
                        , "\"" + split[1] + "\" already has direct access to this lock.");
                return true;
            }
            
            lock.addAccess(split[1]);
            if (lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, MessageLevel.STATUS
                        , "\"" + split[1] + "\" now has direct access to this lock.");
                lock.refresh();
            } else {
                Main.messageManager.respond(sender, MessageLevel.SEVERE
                        , "Unable to add direct access to \"" + split[1] + "\" for this lock.");
            }
            
            return true;
        }
        
        if (action.equals("-access")) {
            if (!(split != null && split[1] != null)) {
                Main.messageManager.respond(sender, MessageLevel.WARNING
                        , "No name specified to remove access from for this lock.");
                return true;
            }
            
            if (!lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, MessageLevel.WARNING
                        , "\"" + split[1] + "\" does not currently have direct access to this lock.");
                return true;
            }
            
            lock.removeAccess(split[1]);
            if (!lock.hasAccess(split[1], true)) {
                Main.messageManager.respond(sender, MessageLevel.STATUS
                        , "\"" + split[1] + "\" has had direct access removed for this lock.");
                lock.refresh();
            } else {
                Main.messageManager.respond(sender, MessageLevel.SEVERE
                        , "Unable to remove direct access for \"" + split[1] + "\" on this lock.");
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
                Main.messageManager.respond(sender, MessageLevel.WARNING
                        , "No name specified to change lock owner to.");
                return true;
            }
            
            lock.removeAccess(split[1]);
            lock.setOwner(split[1]);
            if (lock.isOwner(split[1], true)) {
                Main.messageManager.respond(player, MessageLevel.STATUS
                        , "\"" + split[1] + "\" has been set as the owner for this lock.");
                lock.refresh();
            } else {
                Main.messageManager.respond(sender, MessageLevel.SEVERE
                        , "Unable to set \"" + split[1] + "\" as owner for this lock.");
            }
            
            return true;
        }
        
        if (action.equals("break")) {
            Main.messageManager.log(MessageLevel.FINE
                    , "Lock broken by " + player.getName() + " at "
                    + " x:" + lock.getBlock().getX()
                    + " y:" + lock.getBlock().getY()
                    + " z:" + lock.getBlock().getZ()
            );
            lock.getBlock().setType(Material.AIR);
            lock.getBlock().getWorld().dropItemNaturally(lock.getBlock().getLocation(), new ItemStack(Material.SIGN, 1));
            Main.messageManager.respond(sender, MessageLevel.STATUS, "Lock broken.");
            return true;
        }
        
        if (action.equals("reload")) {
            Main.configurationManager.load();
            this.plugin.readConfiguration();
            Main.messageManager.respond(sender, MessageLevel.STATUS, "Configuration reloaded.");
            return true;
        }
        
        
        return true;
    }
    
    private void actionInfo(Player player, Lock lock) {
        if (!lock.isLock()) {
            Main.messageManager.send(player, MessageLevel.WARNING
                    , "No lock identifed at target.");
            return;
        }
        
        Main.messageManager.send(player, MessageLevel.CONFIG, lock.getDescription());
        
        if (!lock.hasAccess(player)) {
            Main.messageManager.send(player, MessageLevel.RIGHTS
                    , "You do not have access to this lock.");
        } else if (lock.isOwner(player)) {
            Main.messageManager.send(player, MessageLevel.NOTICE
                    , "To modify: /lock (+|-) <Player>");
        }
    }
    
    /**
     * Registers this class as executor for a chat/console command.
     * 
     * @param label Command label to register.
     */
    private void setExecutorOf(String label) {
        PluginCommand command = this.plugin.getCommand(label);
        if (command == null) {
            Main.messageManager.log(MessageLevel.WARNING, "Unable to register \"" + label + "\" command.");
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
    private static String join(String[] s) {
        return join(Arrays.asList(s), " ");
    }
    
    /**
     * Combine all the elements of a list together with a delimiter between each.
     * 
     * @param list List of elements to join.
     * @param delim Delimiter to place between each element.
     * @return String combined with all elements and delimiters.
     */
    private static String join(List<String> list, String delim) {
        if (list == null || list.isEmpty()) return "";
     
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s + delim);
        sb.delete(sb.length() - delim.length(), sb.length());
        
        return sb.toString();
    }
}