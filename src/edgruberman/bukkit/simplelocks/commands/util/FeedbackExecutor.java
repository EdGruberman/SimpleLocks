package edgruberman.bukkit.simplelocks.commands.util;

import org.bukkit.entity.Player;

import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.util.Feedback;

public abstract class FeedbackExecutor extends ConfigurationExecutor {

    protected FeedbackExecutor(final ConfigurationCourier courier) {
        super(courier);
        this.requirePlayer();
    }

    @Override
    protected boolean execute(final ExecutionRequest request) throws CancellationContingency {
        try {
            return super.execute(request);

        } catch (final CancellationContingency c) {
            Feedback.COMMAND_RESULT_FAILURE.send((Player) request.getSender());
            throw c;
        }
    }



}
