package edgruberman.bukkit.simplelocks.commands.util;

import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;

/**
 * supplies {@link ConfigurationCourier} reference,
 * manages display of standard exceptions
 * and formats parameter syntax according to configuration
 * @version 1.2.0
 */
public abstract class ConfigurationExecutor extends Executor {

    protected ConfigurationCourier courier;

    protected ConfigurationExecutor(final ConfigurationCourier courier) {
        this.courier = courier;
    }

    protected void setSyntax(final Parameter.Factory<?, ?, ?> factory) {
        // concatenate known values for limited
        if (factory instanceof LimitedParameter.Factory) {
            final LimitedParameter.Factory<?, ?, ?> limited = (LimitedParameter.Factory<?, ?, ?>) factory;
            final String known = this.joinFactory().elements(limited.known).prefix("argument-syntax-known-").config(this.courier.getBase()).join();
            factory.setSyntax(known);

        // use name for all others
        } else {
            final String name = this.courier.format("argument-syntax-name", factory.name);
            factory.setSyntax(name);
        }

        // reformat for requirement
        final String key = ( factory.required ? "argument-syntax-required" : "argument-syntax-optional" );
        final String requirement = this.courier.format(key, factory.syntax);
        factory.setSyntax(requirement);
    }

    @Override
    protected <P extends Parameter<T>, T> P addParameter(final Parameter.Factory<P, T, ?> factory) {
        this.setSyntax(factory);
        return super.addParameter(factory);
    }

    @Override
    protected boolean validate(final ExecutionRequest request) throws CancellationContingency {
        try {
            return super.validate(request);

        } catch (final SenderRejectedContingency r) {
            final JoinList<String> valid = this.<String>joinFactory().prefix("sender-rejected-valid-").config(this.courier.getBase()).build();
            for (final Class<?> sender : r.getValid()) valid.add(sender.getSimpleName());
            this.courier.send(request.getSender(), "sender-rejected", request.getSender().getClass().getSimpleName(), valid, request.getLabel());
            return false;
        }
    }

    @Override
    protected boolean execute(final ExecutionRequest request) throws CancellationContingency {
        try {
            return this.executeImplementation(request);

        } catch (final MissingArgumentContingency m) {
            this.courier.send(request.getSender(), "argument-missing", m.getParameter().getName(), m.getParameter().getSyntax());
            return false;

        } catch (final UnknownArgumentContingency u) {
            this.courier.send(request.getSender(), "argument-unknown", u.getParameter().getName(), u.getParameter().getSyntax(), u.getArgument());
            return false;
        }
    };

    /** @return true if command processing completed successfully */
    protected abstract boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency;

    protected <Y> ConfigurationJoinListFactory<Y> joinFactory() {
        return new ConfigurationJoinListFactory<Y>().config(this.courier.getBase());
    }

}
