package edgruberman.bukkit.simplelocks;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import edgruberman.bukkit.simplelocks.util.BufferedYamlConfiguration;

public class Aliaser implements Listener {

    private static final int MAXIMUM_TRIES = 100;

    private final Logger logger;
    private final BufferedYamlConfiguration repository;
    private final int length;
    private final String prefix;
    private final Map<String, String> nameToAlias = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); // player name, alias
    private final Map<String, String> aliasToName = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER); // alias, player name

    Aliaser(final Logger logger, final BufferedYamlConfiguration repository, final int length, final String prefix) {
        this.logger = logger;
        this.repository = repository;
        this.length = length;
        this.prefix = prefix;

        for (final String name : repository.getKeys(false)) {
            final String alias = repository.getString(name);
            this.nameToAlias.put(name, alias);
            this.aliasToName.put(alias, name);
        }
    }

    public String getAlias(final String name) {
        return this.nameToAlias.get(name);
    }

    public String alias(final String name) {
        final String result = this.nameToAlias.get(name);
        return ( result != null ? result : name );
    }

    public String getName(final String alias) {
        return this.aliasToName.get(alias);
    }

    public String name(final String alias) {
        final String result = this.aliasToName.get(alias);
        return ( result != null ? result : alias );
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLogin(final PlayerJoinEvent join) {
        if (join.getPlayer().getName().length() <= this.length) return;
        if (this.nameToAlias.containsKey(join.getPlayer().getName())) return;

        String alias = this.prefix + join.getPlayer().getName();
        alias = alias.substring(0, Math.min(alias.length(), this.length));

        // append number when conflict
        if (this.nameToAlias.containsValue(alias)) {
            String found = null;
            for (int i = 1; i < Aliaser.MAXIMUM_TRIES; i++) {
                final String numbered = alias.substring(0, Math.min(alias.length(), this.length - String.valueOf(i).length())) + String.valueOf(i);
                if (!this.nameToAlias.containsValue(numbered)) {
                    found = numbered;
                    break;
                }
            }
            if (found == null) throw new IllegalStateException("Unable to find available alias for " + join.getPlayer().getName());
            alias = found;
        }

        this.nameToAlias.put(join.getPlayer().getName(), alias);
        this.aliasToName.put(alias, join.getPlayer().getName());

        this.repository.set(join.getPlayer().getName(), alias);
        this.repository.queueSave();

        this.logger.log(Level.FINER, "Created {0} alias for {1}", new Object[]{ alias, join.getPlayer().getName() });
    }

}
