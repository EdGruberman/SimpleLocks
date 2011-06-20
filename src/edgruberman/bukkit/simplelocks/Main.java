package edgruberman.bukkit.simplelocks;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.config.Configuration;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    protected static ConfigurationManager configurationManager;
    protected static MessageManager messageManager;
    
    private static String trigger = null;
    private static Configuration configuration;
    
    public void onLoad() {
        Main.configurationManager = new ConfigurationManager(this);
        Main.configurationManager.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configuration = this.getConfiguration();
        }
    
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        this.readConfiguration();
        this.registerEvents();
        new CommandManager(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    protected void readConfiguration() {
        Main.trigger = this.getConfiguration().getString("trigger");
        Main.messageManager.log(MessageLevel.CONFIG, "Lock Trigger: " + Main.trigger);
        
        Lock.setTitle(this.getConfiguration().getString("title"));
        Main.messageManager.log(MessageLevel.CONFIG, "Lock Title: " + Lock.getTitle());
    }
    
    private void registerEvents() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        
        PlayerListener playerListener = new PlayerListener();
        pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        
        BlockListener blockListener = new BlockListener();
        pluginManager.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
        
        EntityListener entityListener = new EntityListener();
        pluginManager.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.Normal, this);
    }
    
    protected static String getDefaultOwner(Player player) {
        return Main.configuration.getString("defaultOwners." + player.getName(), player.getName());
    }
    
    protected static boolean hasTrigger(String line) {
        if (line.length() == 0) return false;
        
        return line.toLowerCase().contains(Main.trigger.toLowerCase());
    }
}
