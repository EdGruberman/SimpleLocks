package edgruberman.bukkit.simplelocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.simplelocks.commands.LockBreak;
import edgruberman.bukkit.simplelocks.commands.LockGrant;
import edgruberman.bukkit.simplelocks.commands.LockInfo;
import edgruberman.bukkit.simplelocks.commands.LockOwner;
import edgruberman.bukkit.simplelocks.commands.LockRevoke;
import edgruberman.bukkit.simplelocks.commands.Reload;
import edgruberman.bukkit.simplelocks.messaging.couriers.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.messaging.couriers.TimestampedConfigurationCourier;

public class Main extends JavaPlugin {

    public static ConfigurationCourier courier;

    private static final Version MINIMUM_CONFIGURATION = new Version("2.0.0");

    private Locksmith locksmith = null;

    @Override
    public void onEnable() {
        this.reloadConfig();

        Main.courier =new TimestampedConfigurationCourier(this, "messages");

        final String title = this.getConfig().getString("title");
        this.getLogger().config("Lock title: " + title);
        if (title.length() < 1 || title.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH)
            throw new IllegalArgumentException("Lock title must be between 1 and " + Locksmith.MAXIMUM_SIGN_LINE_LENGTH + " characters");

        this.locksmith = new Locksmith(this, title);

        this.getCommand("simplelocks:lock.info").setExecutor(new LockInfo(this.locksmith));
        this.getCommand("simplelocks:lock.grant").setExecutor(new LockGrant(this.locksmith));
        this.getCommand("simplelocks:lock.revoke").setExecutor(new LockRevoke(this.locksmith));
        this.getCommand("simplelocks:lock.owner").setExecutor(new LockOwner(this.locksmith));
        this.getCommand("simplelocks:lock.break").setExecutor(new LockBreak(this.locksmith));
        this.getCommand("simplelocks:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));

        final Version version = new Version(this.getConfig().isSet("version") ? this.getConfig().getString("version") : null);
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
    }

    private void archiveConfig(final String resource, final Version version) {
        final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
        final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
        final File existing = new File(this.getDataFolder(), resource);

        if (!existing.renameTo(backup))
            throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");

        this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
    }

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            super.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final char[] cbuf = new char[1024]; int read;
        try {
            final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
            while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
