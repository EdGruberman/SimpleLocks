package edgruberman.bukkit.simplelocks;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.event.HandlerList;

import edgruberman.bukkit.simplelocks.commands.Break;
import edgruberman.bukkit.simplelocks.commands.Describe;
import edgruberman.bukkit.simplelocks.commands.Grant;
import edgruberman.bukkit.simplelocks.commands.Reload;
import edgruberman.bukkit.simplelocks.commands.Revoke;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.util.BufferedYamlConfiguration;
import edgruberman.bukkit.simplelocks.util.CustomPlugin;
import edgruberman.bukkit.simplelocks.util.Feedback;

public class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    @Override
    public void onLoad() {
        this.putConfigMinimum("3.4.0a0");
        this.putConfigMinimum("language.yml", "3.4.0a0");
        Feedback.register(this, this.getServer().getScheduler());
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Main.courier = ConfigurationCourier.Factory.create(this).setBase(this.loadConfig("language.yml")).setFormatCode("format-code").build();

        // title
        final String title = this.getConfig().getString("title");
        this.getLogger().log(Level.CONFIG, "Lock title: {0}", title);
        if (title.length() < 1 || title.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH) {
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Lock title must be between 1 and {0} characters", Locksmith.MAXIMUM_SIGN_LINE_LENGTH);
            this.setEnabled(false);
            return;
        }

        // aliases
        final BufferedYamlConfiguration aliases = new BufferedYamlConfiguration(this, new File(this.getDataFolder(), "aliases.yml"), 3000);
        try {
            aliases.load();
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Disabling plugin; Unable to load aliases.yml; " + e);
            this.setEnabled(false);
            return;
        }
        final Aliaser aliaser = new Aliaser(this.getLogger(), aliases, this.getConfig().getInt("auto-alias.length"), this.getConfig().getString("auto-alias.prefix"));
        if (this.getConfig().getBoolean("auto-alias.enabled")) this.getServer().getPluginManager().registerEvents(aliaser, this);

        // locksmith
        final Locksmith locksmith = new Locksmith(this, title, aliaser, this.getConfig().getStringList("permissions"));

        // explosive protection
        if (this.getConfig().getBoolean("explosion-protection")) new ExplosiveOrdnanceDisposal(this, locksmith);

        // commands
        this.getCommand("simplelocks:describe").setExecutor(new Describe(locksmith));
        this.getCommand("simplelocks:grant").setExecutor(new Grant(locksmith, aliaser));
        this.getCommand("simplelocks:revoke").setExecutor(new Revoke(locksmith, aliaser));
        this.getCommand("simplelocks:break").setExecutor(new Break(locksmith));
        this.getCommand("simplelocks:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Main.courier = null;
    }

}
