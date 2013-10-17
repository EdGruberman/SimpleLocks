package edgruberman.bukkit.simplelocks.commands.util;

import org.bukkit.configuration.ConfigurationSection;

/** @version 1.0.0 */
public class ConfigurationJoinListFactory<T> extends JoinList.Factory<T, ConfigurationJoinListFactory<T>> {

    public static final String DEFAULT_PREFIX = "";
    public static final ConfigurationSection DEFAULT_CONFIG = null;

    public static final String CONFIG_KEY_FORMAT = "format";
    public static final String CONFIG_KEY_ITEM = "item";
    public static final String CONFIG_KEY_DELIMITER = "delimiter";
    public static final String CONFIG_KEY_LAST = "last";

    public static <Y> ConfigurationJoinListFactory<Y> create() {
        return new ConfigurationJoinListFactory<Y>();
    }

    protected String prefix = ConfigurationJoinListFactory.DEFAULT_PREFIX;
    protected ConfigurationSection config = ConfigurationJoinListFactory.DEFAULT_CONFIG;

    public ConfigurationJoinListFactory<T> prefix(final String prefix) {
        this.prefix = prefix;
        return this.cast();
    }

    public ConfigurationJoinListFactory<T> config(final ConfigurationSection config) {
        this.config = config;
        return this.cast();
    }

    @Override
    public ConfigurationJoinListFactory<T> cast() {
        return this;
    }

    @Override
    public JoinList<T> build() {
        if (this.config != null) {
            this.format = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_FORMAT, this.format);
            this.item = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_ITEM, this.item);
            this.delimiter = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_DELIMITER, this.delimiter);
            this.last = this.config.getString(this.prefix + ConfigurationJoinListFactory.CONFIG_KEY_LAST, this.delimiter);
        }

        return super.build();
    }

}
