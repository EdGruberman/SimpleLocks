package edgruberman.bukkit.simplelocks.commands;

import java.util.Locale;

import edgruberman.bukkit.simplelocks.Aliaser;
import edgruberman.bukkit.simplelocks.commands.util.CancellationContingency;
import edgruberman.bukkit.simplelocks.commands.util.ConfigurationExecutor;
import edgruberman.bukkit.simplelocks.commands.util.ExecutionRequest;
import edgruberman.bukkit.simplelocks.commands.util.LowerCaseParameter;
import edgruberman.bukkit.simplelocks.commands.util.MissingArgumentContingency;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;

public class Alias extends ConfigurationExecutor {

    private final Aliaser aliaser;

    private final LowerCaseParameter name;

    public Alias(final ConfigurationCourier courier, final Aliaser aliaser) {
        super(courier);
        this.aliaser = aliaser;

        this.name = this.addOptional(LowerCaseParameter.Factory.create("player|alias"));
    }

    // Usage: /<command> [player|alias]
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency {
        if (!request.isPlayer() && !request.isExplicit(this.name)) throw new MissingArgumentContingency(request, this.name);
        final String name = ( request.isExplicit(this.name) ? request.parse(this.name) : request.getSender().getName().toLowerCase(Locale.ENGLISH) );

        final String alias = this.aliaser.getAlias(name);
        if (alias != null) {
            this.courier.send(request.getSender(), "alias-alias", name, alias);
            return true;
        }

        final String player = this.aliaser.getName(name);
        if (player != null) {
            this.courier.send(request.getSender(), "alias-player", name, player);
            return true;
        }

        this.courier.send(request.getSender(), "alias-none", name);
        return true;
    }

}
