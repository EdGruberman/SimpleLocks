package edgruberman.bukkit.simplelocks;

import org.bukkit.entity.Player;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    private static String trigger = null;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
    
    public void onEnable() {
        this.readConfiguration();
        new PlayerListener(this);
        new BlockListener(this);
        new EntityListener(this);
        new CommandManager(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    void readConfiguration() {
        Main.trigger = this.getConfiguration().getString("trigger");
        Main.messageManager.log("Lock Trigger: " + Main.trigger, MessageLevel.CONFIG);
        
        Lock.setTitle(this.getConfiguration().getString("title"));
        Main.messageManager.log("Lock Title: " + Lock.getTitle(), MessageLevel.CONFIG);
    }
    
    static String getDefaultOwner(Player player) {
        return Main.configurationFile.getConfiguration().getString("defaultOwners." + player.getName(), player.getName());
    }
    
    static boolean hasTrigger(String line) {
        if (line.length() == 0) return false;
        
        return line.toLowerCase().contains(Main.trigger.toLowerCase());
    }
}
