package edgruberman.bukkit.simplelocks;

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
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.configurationFile = new ConfigurationFile(this);
    }

    @Override
    public void onEnable() {
        this.loadConfiguration();

        new PlayerListener(this);
        new BlockListener(this);
        new EntityListener(this);
        new CommandManager(this);
    }

    void loadConfiguration() {
        final FileConfiguration config = Main.configurationFile.load();

        Main.trigger = config.getString("trigger");
        Main.messageManager.log("Lock Trigger: " + Main.trigger, MessageLevel.CONFIG);

        Lock.setTitle(config.getString("title"));
        Main.messageManager.log("Lock Title: " + Lock.getTitle(), MessageLevel.CONFIG);
    }

    static String getDefaultOwner(final Player player) {
        return Main.configurationFile.getConfig().getString("defaultOwners." + player.getName(), player.getName());
    }

    static boolean hasTrigger(final String line) {
        if (line.length() == 0) return false;

        return line.toLowerCase().contains(Main.trigger.toLowerCase());
    }

}
