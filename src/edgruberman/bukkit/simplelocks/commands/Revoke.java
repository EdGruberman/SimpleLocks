package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Aliaser;
import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.commands.util.CancellationContingency;
import edgruberman.bukkit.simplelocks.commands.util.ExecutionRequest;
import edgruberman.bukkit.simplelocks.commands.util.FeedbackExecutor;
import edgruberman.bukkit.simplelocks.commands.util.OfflinePlayerParameter;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.util.Feedback;

public class Revoke extends FeedbackExecutor {

    private final Locksmith locksmith;
    private final Aliaser aliaser;

    private final OfflinePlayerParameter name;

    public Revoke(final ConfigurationCourier courier, final Server server, final Locksmith locksmith, final Aliaser aliaser) {
        super(courier);
        this.locksmith = locksmith;
        this.aliaser = aliaser;

        this.name = this.addRequired(OfflinePlayerParameter.Factory.create("name", server));
    }

    // usage: /<command> name
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency {
        final Player player = (Player) request.getSender();

        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            this.courier.send(request.getSender(), "requires-lock");
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        if (!lock.hasAccess(player)) {
            final List<String> names = this.<String>joinFactory().prefix("access-").elements(lock.accessNames()).build();
            this.courier.send(request.getSender(), "requires-access", request.getLabel(), names);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        final String name = this.aliaser.alias(request.parse(this.name).getName());
        if (!lock.hasExplicitAccess(name)) {
            this.courier.send(request.getSender(), "revoke-missing", name);
            Feedback.COMMAND_RESULT_WARNING.send(player);
            return true;
        }

        lock.removeAccess(name);
        if (!lock.hasAccess(player)){
            this.courier.send(request.getSender(), "revoke-prevent");
            lock.addAccess(name);
            Feedback.COMMAND_RESULT_WARNING.send(player);
            return true;
        }

        this.courier.send(request.getSender(), "revoke-success", name);
        Feedback.COMMAND_RESULT_SUCCESS.send(player);
        return true;
    }

}
