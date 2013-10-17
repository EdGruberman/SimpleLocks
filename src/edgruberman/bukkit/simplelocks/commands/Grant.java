package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

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

public class Grant extends FeedbackExecutor {

    private final Locksmith locksmith;
    private final Aliaser aliaser;

    private final OfflinePlayerParameter name;

    public Grant(final ConfigurationCourier courier, final Server server, final Locksmith locksmith, final Aliaser aliaser) {
        super(courier);
        this.locksmith = locksmith;
        this.aliaser = aliaser;

        this.name = this.addRequired(OfflinePlayerParameter.Factory.create("name", server));
    }

    // usage: /<command> name
    @Override
    protected boolean executeImplementation(final ExecutionRequest request) throws CancellationContingency {
        final Player player = (Player) request.getSender();

        @SuppressWarnings("deprecation") // no valid alternative
        final Lock lock = this.locksmith.findLock(player.getTargetBlock((HashSet<Byte>) null, 4));
        if (lock == null) {
            this.courier.send(request.getSender(), "requires-lock");
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        if (!lock.hasAccess(player)) {
            this.courier.send(request.getSender(), "requires-access", request.getLabel());
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        final String name = this.aliaser.alias(request.parse(this.name).getName());
        if (name.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH) {
            this.courier.send(request.getSender(), "requires-alias", name, name.length(), Locksmith.MAXIMUM_SIGN_LINE_LENGTH);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        if (lock.hasExplicitAccess(name)) {
            this.courier.send(request.getSender(), "grant-already", name);
            Feedback.COMMAND_RESULT_WARNING.send(player);
            return true;
        }

        if (lock.getAccess().size() == 3) {
            this.courier.send(request.getSender(), "grant-full", name);
            Feedback.COMMAND_RESULT_FAILURE.send(player);
            return true;
        }

        lock.addAccess(name);
        this.courier.send(request.getSender(), "grant-success", name);
        Feedback.COMMAND_RESULT_SUCCESS.send(player);
        return true;
    }

}
