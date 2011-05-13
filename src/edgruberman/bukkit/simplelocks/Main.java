package edgruberman.bukkit.simplelocks;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    public static MessageManager messageManager;
    
    private static String trigger = null;
    
    public void onLoad() {
        Configuration.load(this);
    }
    
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
               
        Main.trigger = this.getConfiguration().getString("trigger");
        Main.messageManager.log(MessageLevel.CONFIG, "Lock Trigger: " + Main.trigger);
        
        Lock.setTitle(this.getConfiguration().getString("title"));
        Main.messageManager.log(MessageLevel.CONFIG, "Lock Title: " + Lock.getTitle());
        
        this.registerEvents();
        
        this.getCommand("lock").setExecutor(new CommandManager());
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        //TODO Unregister listeners when Bukkit supports it.
        
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
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
    
    public static boolean hasTrigger(String line) {
        if (line.length() == 0) return false;
        
        return line.toLowerCase().contains(Main.trigger.toLowerCase());
    }
}
