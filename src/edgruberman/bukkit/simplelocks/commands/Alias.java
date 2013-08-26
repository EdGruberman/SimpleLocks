package edgruberman.bukkit.simplelocks.commands;

import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Aliaser;
import edgruberman.bukkit.simplelocks.Main;

public class Alias implements CommandExecutor {

    private final Aliaser aliaser;

    public Alias(final Aliaser aliaser) {
        this.aliaser = aliaser;
    }

    // Usage: /<command> [player|alias]
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1 && !(sender instanceof Player)) {
            Main.courier.send(sender, "requires-argument", "name", 0);
            return false;
        }
        final String name = ( args.length >= 1 ? args[0].toLowerCase(Locale.ENGLISH) : sender.getName().toLowerCase(Locale.ENGLISH) );

        final String alias = this.aliaser.getAlias(name);
        if (alias != null) {
            Main.courier.send(sender, "alias.alias", name, alias);
            return true;
        }

        final String player = this.aliaser.getName(name);
        if (player != null) {
            Main.courier.send(sender, "alias.player", name, player);
            return true;
        }

        Main.courier.send(sender, "alias.none", name);
        return true;
    }

}
