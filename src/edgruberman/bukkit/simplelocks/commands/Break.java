package edgruberman.bukkit.simplelocks.commands;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.simplelocks.Lock;
import edgruberman.bukkit.simplelocks.Locksmith;
import edgruberman.bukkit.simplelocks.commands.util.CancellationContingency;
import edgruberman.bukkit.simplelocks.commands.util.ExecutionRequest;
import edgruberman.bukkit.simplelocks.commands.util.FeedbackExecutor;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;
import edgruberman.bukkit.simplelocks.util.Feedback;

public class Break extends FeedbackExecutor {

    private final Locksmith locksmith;

    public Break(final ConfigurationCourier courier, final Locksmith locksmith) {
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

        request.getSender().getServer().dispatchCommand(request.getSender(), "simplelocks:describe");

        lock.sign.setType(Material.AIR);
        lock.sign.update(true);
        lock.sign.getWorld().dropItemNaturally(lock.sign.getLocation(), new ItemStack(Material.SIGN, 1));
        this.courier.send(request.getSender(), "break");
        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
        return true;
    }

}
