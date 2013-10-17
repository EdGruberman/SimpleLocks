package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.commands.util.CancellationContingency;
import edgruberman.bukkit.simplelocks.commands.util.ExecutionRequest;
import edgruberman.bukkit.simplelocks.commands.util.FeedbackExecutor;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.util.Feedback;

public class Describe extends FeedbackExecutor {

    private final Locksmith locksmith;

    public Describe(final ConfigurationCourier courier, final Locksmith locksmith) {
        super(courier);
        this.locksmith = locksmith;
    }

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

        final List<String> names = this.<String>joinFactory().prefix("access-").elements(lock.accessNames()).build();
        this.courier.send(request.getSender(), "describe", names, lock.hasAccess(player)?1:0);
        return true;
    }

}
