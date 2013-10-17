package edgruberman.bukkit.simplelocks;

import java.io.File;
import java.util.logging.Level;

import edgruberman.bukkit.simplelocks.commands.Alias;
import edgruberman.bukkit.simplelocks.commands.Break;
import edgruberman.bukkit.simplelocks.commands.Describe;
import edgruberman.bukkit.simplelocks.commands.Grant;
import edgruberman.bukkit.simplelocks.commands.Reload;
import edgruberman.bukkit.simplelocks.commands.Revoke;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.util.BufferedYamlConfiguration;
import edgruberman.bukkit.simplelocks.util.Feedback;
import edgruberman.bukkit.simplelocks.util.StandardPlugin;

public class Main extends StandardPlugin {

    static final String FILE_LANGUAGE = "language.yml";

    @Override
    public void onLoad() {
        this.putDefinition(StandardPlugin.CONFIGURATION_FILE, Configuration.getDefinition(StandardPlugin.CONFIGURATION_FILE));
        this.putDefinition(Main.FILE_LANGUAGE, Configuration.getDefinition(Main.FILE_LANGUAGE));
        Feedback.register(this, this.getServer().getScheduler());
    }

    @Override
    public void onEnable() {
        this.reloadConfig();
        final ConfigurationCourier courier = ConfigurationCourier.Factory.create(this).setBase(this.loadConfig(Main.FILE_LANGUAGE)).setFormatCode("format-code").build();

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
        final Locksmith locksmith = new Locksmith(courier, this.getLogger(), title, aliaser, this.getConfig().getStringList("permissions"));
        this.getServer().getPluginManager().registerEvents(locksmith, this);

        // explosive protection
        if (this.getConfig().getBoolean("explosion-protection")) new ExplosiveOrdnanceDisposal(this, locksmith);

        // commands
        this.getCommand("simplelocks:describe").setExecutor(new Describe(courier, locksmith));
        this.getCommand("simplelocks:grant").setExecutor(new Grant(courier, this.getServer(), locksmith, aliaser));
        this.getCommand("simplelocks:revoke").setExecutor(new Revoke(courier, this.getServer(), locksmith, aliaser));
        this.getCommand("simplelocks:alias").setExecutor(new Alias(courier, aliaser));
        this.getCommand("simplelocks:break").setExecutor(new Break(courier, locksmith));
        this.getCommand("simplelocks:reload").setExecutor(new Reload(courier, this));
    }

}
