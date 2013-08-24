package edgruberman.bukkit.simplelocks;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import edgruberman.bukkit.simplelocks.util.BufferedYamlConfiguration;

public class Aliaser implements Listener {

    private static final int MAXIMUM_TRIES = 100;

    private final Logger logger;
    private final BufferedYamlConfiguration repository;
    private final int length;
    private final String prefix;
    private final Map<String, String> aliases = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); // player name, alias
    private final Map<String, String> names = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); // alias, player name

    Aliaser(final Logger logger, final BufferedYamlConfiguration repository, final int length, final String prefix) {
        this.logger = logger;
        this.repository = repository;
        this.length = length;
        this.prefix = prefix;

        for (final String name : repository.getKeys(false)) {
            this.aliases.put(name, repository.getString(name));
        }
    }

    public String getAlias(final String name) {
        final String result = this.aliases.get(name);
        return ( result != null ? result : name );
    }

    public String getName(final String alias) {
        final String result = this.names.get(alias);
        return ( result != null ? result : alias );
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(final PlayerLoginEvent login) {
        if (login.getPlayer().getName().length() <= this.length) return;
        if (this.aliases.containsKey(login.getPlayer().getName())) return;

        String alias = this.prefix + login.getPlayer().getName();
        alias = alias.substring(0, Math.min(alias.length(), this.length));

        // append number when conflict
        if (this.aliases.containsKey(alias)) {
            String found = null;
            for (int i = 1; i < Aliaser.MAXIMUM_TRIES; i++) {
                final String numbered = alias.substring(0, Math.min(alias.length(), this.length - String.valueOf(i).length())) + String.valueOf(i);
                if (!this.aliases.containsKey(alias)) {
                    found = numbered;
                    break;
                }
            }
            if (found == null) throw new IllegalStateException("Unable to find available alias for " + login.getPlayer().getName());
            alias = found;
        }

        this.aliases.put(login.getPlayer().getName(), alias);
        this.repository.queueSave();

        this.logger.log(Level.FINER, "Created {0} alias for {1}", new Object[]{ alias, login.getPlayer().getName() });
    }

}
