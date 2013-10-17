package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edgruberman.bukkit.simplelocks.configuration.ConfigurationDefinition;
import edgruberman.bukkit.simplelocks.configuration.ConfigurationInstruction;
import edgruberman.bukkit.simplelocks.configuration.HeaderInstruction;
import edgruberman.bukkit.simplelocks.configuration.PutKeyInstruction;
import edgruberman.bukkit.simplelocks.configuration.RemoveKeyInstruction;
import edgruberman.bukkit.simplelocks.util.StandardPlugin;
import edgruberman.bukkit.simplelocks.versioning.StandardVersion;

/** organizes configuration file definitions into a separate class container */
public final class Configuration {

    private static Map<String, ConfigurationDefinition> definitions = new HashMap<String, ConfigurationDefinition>();

    static {
        Configuration.definitions.put(StandardPlugin.CONFIGURATION_FILE, Configuration.defineConfig());
        Configuration.definitions.put(Main.FILE_LANGUAGE, Configuration.defineLanguage());
    }

    public static ConfigurationDefinition getDefinition(final String name) {
        return Configuration.definitions.get(name);
    }

    private static ConfigurationDefinition defineConfig() {
        final ConfigurationDefinition result = new ConfigurationDefinition();

        final List<ConfigurationInstruction> v3_4_0 = result.createInstructions(StandardVersion.parse("3.4.0"));

        v3_4_0.add(PutKeyInstruction.create(StandardPlugin.DEFAULT_LOG_LEVEL.getName(), StandardPlugin.KEY_LOG_LEVEL));
        v3_4_0.add(PutKeyInstruction.create("§1++ LOCKED ++", "title"));
        v3_4_0.add(PutKeyInstruction.create(Arrays.asList("server.user.{0}", "server.group.{0}"), "permissions"));
        v3_4_0.add(PutKeyInstruction.create(true, "auto-alias", "enabled"));
        v3_4_0.add(PutKeyInstruction.create(15, "auto-alias", "length"));
        v3_4_0.add(PutKeyInstruction.create("~", "auto-alias", "prefix"));
        v3_4_0.add(PutKeyInstruction.create(true, "explosion-protection"));

        v3_4_0.add(RemoveKeyInstruction.create("version"));

        return result;
    }

    private static ConfigurationDefinition defineLanguage() {
        final ConfigurationDefinition result = new ConfigurationDefinition();

        final List<ConfigurationInstruction> v3_4_0 = result.createInstructions(StandardVersion.parse("3.4.0"));

        v3_4_0.add(HeaderInstruction.create(
                "\n"
                + "---- arguments ----\n"
                + "region-cancel-create: 0 = generated, 1 = region\n"
                + "denied: 0 = generated, 1 = access\n"
                + "describe: 0 = generated, 1 = access, 2 = hasAccess(0|1)\n"
                + "grant-success: 0 = generated, 1 = name\n"
                + "grant-already: 0 = generated, 1 = name\n"
                + "grant-full: 0 = generated\n"
                + "revoke-success: 0 = generated, 1 = name\n"
                + "revoke-missing: 0 = generated, 1 = name\n"
                + "alias-alias: 0 = generated, 1 = name, 2 = alias\n"
                + "alias-player: 0 = generated, 1 = name, 2 = player\n"
                + "alias-none: 0 = generated, 1 = name\n"
                + "requires-alias: 0 = generated, 1 = name, 2 = length, 3 = maximum\n"
                + "requires-access: 0 = generated, 1 = label\n"
                + "reload: 0 = generated, 1 = plugin name\n"
                + "sender-rejected: 0 = generated, 1 = acceptable, 2 = label\n"
                + "sender-rejected-valid-item: 0 = name\n"
                + "argument-missing: 0 = generated, 1 = name 2 = syntax\n"
                + "argument-unknown: 0 = generated, 1 = name, 2 = syntax, 3 = value\n"
                + "argument-syntax-name: 0 = name\n"
                + "argument-syntax-known-item: 0 = value\n"
                + "argument-syntax-required: 0 = argument\n"
                + "argument-syntax-optional: 0 = argument\n"
                + "\n"
                + "---- patterns ----"
        ));

        v3_4_0.add(PutKeyInstruction.create("§", "format-code"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §7Locks §ccan not be created§7 in region: §f{1}", "region-cancel-create"));

        v3_4_0.add(PutKeyInstruction.create("§8, §7", "access-delimiter"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock §caccess denied §8(Access: §7{1}§8)", "denied"));
        v3_4_0.add(PutKeyInstruction.create("§f-> {2,choice,0#§e|1#§2}Lock Access§8: §7{1}{2,choice,0#|1#' §8(§7To add: §b/lock+ §3§oname§8)'}", "describe"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock §2broken", "break"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock §2access granted§7 explicitly to §f{1}", "grant-success"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock access §ealready granted§7 explicitly to §f{1}", "grant-already"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock §caccess full", "grant-full"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock explicit §2access revoked§7 from §f{1}", "revoke-success"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7You §ecan not remove§7 your own access to a lock", "revoke-prevent"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7Lock access §enot explicitly granted §7to §f{1}", "revoke-missing"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §7Alias for §f{1} §7is §f{2}", "alias-alias"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7Player for §f{1} §7is §f{2}", "alias-player"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §7No alias or player found for §f{1}", "alias-none"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §2Reloaded §7{1} plugin", "reload"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §cNo lock§7 identified", "requires-lock"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §cOnly {1} §7can use the §b/{2} §7command", "sender-rejected"));
        v3_4_0.add(PutKeyInstruction.create("§c{0}s", "sender-rejected-valid-item"));
        v3_4_0.add(PutKeyInstruction.create("§4,", "sender-rejected-valid-delimiter"));

        v3_4_0.add(PutKeyInstruction.create("§f-> §cMissing §7required argument§8: {2}", "argument-missing"));
        v3_4_0.add(PutKeyInstruction.create("§f-> §cUnknown§7 argument for {2}§8: §f{3}", "argument-unknown"));
        v3_4_0.add(PutKeyInstruction.create("§3§o{0}", "argument-syntax-name"));
        v3_4_0.add(PutKeyInstruction.create("§b{0}", "argument-syntax-known-item"));
        v3_4_0.add(PutKeyInstruction.create("§3|", "argument-syntax-known-delimiter"));
        v3_4_0.add(PutKeyInstruction.create("{0}", "argument-syntax-required"));
        v3_4_0.add(PutKeyInstruction.create("§3[{0}§3]", "argument-syntax-optional"));

        v3_4_0.add(RemoveKeyInstruction.create("grant"));
        v3_4_0.add(RemoveKeyInstruction.create("revoke"));
        v3_4_0.add(RemoveKeyInstruction.create("alias"));
        v3_4_0.add(RemoveKeyInstruction.create("access"));

        v3_4_0.add(RemoveKeyInstruction.create("version"));

        return result;
    }

}
