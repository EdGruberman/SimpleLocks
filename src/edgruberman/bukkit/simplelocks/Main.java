package edgruberman.bukkit.simplelocks;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends JavaPlugin {

    private static Level DEFAULT_LOGGING_LEVEL = Level.INFO;

    static MessageManager messageManager;
    Locksmith locksmith = null;

    @Override
    public void onEnable() {
        if (!new File(this.getDataFolder(), "config.yml").isFile()) this.saveDefaultConfig();
        this.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"), Main.DEFAULT_LOGGING_LEVEL);

        Main.messageManager = new MessageManager(this);

        this.start(this, this.getConfig());
    }

    @Override
    public void onDisable() {
        this.locksmith = null;
    }

    public void start(final JavaPlugin context, final ConfigurationSection config) {
        this.locksmith = new Locksmith(context);
        this.locksmith.setTitle(config.getString("title"));
        context.getLogger().config("Lock Title: " + this.locksmith.getTitle());

        if (!config.isConfigurationSection("defaultOwners")) config.createSection("defaultOwners");
        this.locksmith.defaultOwners = config.getConfigurationSection("defaultOwners");

        // TODO update command manager; instantiate individual commands
        new CommandManager(context, this.locksmith);
    }

    private void setLogLevel(final String name, final Level defaultLevel) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Main.DEFAULT_LOGGING_LEVEL;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
