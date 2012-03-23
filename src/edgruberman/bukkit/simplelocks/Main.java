package edgruberman.bukkit.simplelocks;

import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends JavaPlugin {

    static MessageManager messageManager;

    private static ConfigurationFile configurationFile;
    private static String trigger = null;

    @Override
    public void onEnable() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        this.setLoggingLevel();

        Main.messageManager = new MessageManager(this);

        this.configure();

        new PlayerListener(this);
        new BlockListener(this);
        new EntityListener(this);
        new CommandManager(this);
    }

    private void setLoggingLevel() {
        final String name = Main.configurationFile.getConfig().getString("logLevel", "INFO");
        Level level = MessageLevel.parse(name);
        if (level == null) level = Level.INFO;

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + this.getLogger().getLevel());
    }

    void configure() {
        final FileConfiguration config = Main.configurationFile.getConfig();

        Main.trigger = config.getString("trigger");
        this.getLogger().log(Level.CONFIG, "Lock Trigger: " + Main.trigger);

        Lock.setTitle(config.getString("title"));
        this.getLogger().log(Level.CONFIG, "Lock Title: " + Lock.getTitle());
    }

    static String getDefaultOwner(final Player player) {
        return Main.configurationFile.getConfig().getString("defaultOwners." + player.getName(), player.getName());
    }

    static boolean hasTrigger(final String line) {
        if (line.length() == 0) return false;

        return line.toLowerCase().contains(Main.trigger.toLowerCase());
    }

}
