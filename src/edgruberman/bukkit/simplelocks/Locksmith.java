package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import edgruberman.bukkit.simplelocks.commands.util.ConfigurationJoinListFactory;
import edgruberman.bukkit.simplelocks.messaging.Courier.ConfigurationCourier;

public class Locksmith implements Listener {

    public static final int MAXIMUM_SIGN_LINE_LENGTH = 15;

    private static final List<BlockFace> CARDINALS = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    /** text on the first line of the sign that indicates it is a lock */
    public final String title;

    private final ConfigurationCourier courier;
    private final Logger logger;
    private final Aliaser aliaser;
    private final List<String> permissions;

    Locksmith(final ConfigurationCourier courier, final Logger logger, final String title, final Aliaser aliaser, final List<String> permissions) {
        this.courier = courier;
        this.logger = logger;
        this.title = title;
        this.aliaser = aliaser;
        this.permissions = permissions;
    }

    /**
     * Create new lock
     *
     * @param block where to create sign containing lock information
     * @param attached face adjacent to lockable
     * @param owner player name or group name
     */
    Lock createLock(final Block block, final BlockFace attached, final String owner) {
        final Block locked = block.getRelative(attached);
        if (this.isLocked(locked)) throw new IllegalStateException("block already locked: " + locked);

        // configure material
        final org.bukkit.material.Sign material = new org.bukkit.material.Sign(Material.WALL_SIGN);
        material.setFacingDirection(attached.getOppositeFace());
        block.setTypeIdAndData(material.getItemTypeId(), material.getData(), true);

        // change first line to lock title
        final Sign sign = (Sign) block.getState();
        sign.setLine(0, this.title);

        final Lock lock = new Lock(sign, this.aliaser, this.permissions);
        lock.addAccess(owner); // updates block for previous state changes also

        return lock;
    }

    /**
     * Indicates if block is a lock for any block
     *
     * @param block block to check
     * @return true if lock; false otherwise
     */
    boolean isLock(final Block block) {
        return this.isLock(block, null);
    }

    /**
     * Indicates if block is a lock
     *
     * @param block block to check
     * @param attached face connected to locked object (null to determine if block is a lock for any block)
     * @return true if lock; false otherwise
     */
    private boolean isLock(final Block block, final BlockFace attached) {
        // Locks must be a wall sign
        if (block.getTypeId() != Material.WALL_SIGN.getId()) return false;

        // Locks must be directly attached to locked block
        if (attached != null) {
            final org.bukkit.material.Sign material = (org.bukkit.material.Sign) block.getState().getData();
            if (!material.getAttachedFace().equals(attached)) return false;
        }

        // First line of sign must contain standard lock title
        if (!((org.bukkit.block.Sign) block.getState()).getLine(0).equals(this.title)) return false;

        return true;
    }

    /**
     * Indicates if block is actively locked by a lock, but not a lock itself.
     *
     * @param block block to check
     * @return true if block is locked; false otherwise
     */
    boolean isLocked(final Block block) {
        return this.findChestLock(block) != null;
    }

    /**
     * Find lock for block, which could be the block itself
     *
     * @param block block to check
     * @return Lock associated with block; null if none
     */
    public Lock findLock(final Block block) {
        if (this.isLock(block)) return new Lock((Sign) block.getState(), this.aliaser, this.permissions);

        final Block lock = this.findChestLock(block);
        if (lock != null) return new Lock((Sign) lock.getState(), this.aliaser, this.permissions);

        return null;
    }

    /**
     * Returns the lock associated with the chest
     *
     * @param chest block (small or either side of a large)
     * @return Block containing lock associated to chest; null if none
     */
    private Block findChestLock(final Block chest) {
        if (chest.getTypeId() != Material.CHEST.getId()) return null;

        // check directly adjacent blocks for lock
        for (final BlockFace direction : Locksmith.CARDINALS) {
            final Block relative = chest.getRelative(direction);

            if (relative.getTypeId() == Material.CHEST.getId()) {
                // found double chest - check directly adjacent blocks to second half of chest for lock
                for (final BlockFace direction2 : Locksmith.CARDINALS) {
                    final Block relative2 = relative.getRelative(direction2);
                    if (this.isLock(relative2, direction2.getOppositeFace())) return relative2;
                }

            } else {
                if (this.isLock(relative, direction.getOppositeFace())) return relative;
            }
        }

        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent interaction) {

        // left clicking a lock or locked block will describe the lock
        if (interaction.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            final Lock lock = this.findLock(interaction.getClickedBlock());
            if (lock == null) return;

            final List<String> names = ConfigurationJoinListFactory.<String>create().config(this.courier.getBase()).prefix("access-").elements(lock.accessNames()).build();
            this.courier.send(interaction.getPlayer(), "describe", names, lock.hasAccess(interaction.getPlayer())?1:0);
            return;
        }

        if (!interaction.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !interaction.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        final Lock lock = this.findLock(interaction.getClickedBlock());
        if (lock != null) {
            // existing lock found

            if (!lock.hasAccess(interaction.getPlayer())) {
                // player does not have access, cancel interaction and notify player
                interaction.setCancelled(true);
                this.courier.send(interaction.getPlayer(), "denied", lock.accessNames());
                interaction.getPlayer().playSound(interaction.getPlayer().getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);
                this.logger.log(Level.FINEST, "Lock access denied to {0} at {1}", new Object[] { interaction.getPlayer().getName(), interaction.getClickedBlock() });
                return;
            }

            // player has access, if they did not click directly on lock, let things proceed as normal
            if (interaction.getClickedBlock().getType() != Material.WALL_SIGN) return;

            // player has access and they right clicked on lock, pass through to locked chest
            final Chest chest = (Chest) lock.getLocked().getState();
            interaction.getPlayer().openInventory(chest.getInventory());
            interaction.setUseItemInHand(Result.DENY);
            interaction.setUseInteractedBlock(Result.DENY);
            return;
        }

        // no existing lock, check to see if player is requesting a lock be created
        // must be holding a sign and right click on a vertical face of the chest that is adjacent to air
        if (!interaction.getPlayer().hasPermission("simplelocks.create")) return;
        if (interaction.getClickedBlock().getType() != Material.CHEST || interaction.getMaterial() != Material.SIGN) return;
        if (interaction.getBlockFace() == BlockFace.UP || interaction.getBlockFace() == BlockFace.DOWN) return;
        if (interaction.getClickedBlock().getRelative(interaction.getBlockFace()).getType() != Material.AIR) return;

        // give other plugins a chance to cancel this sign creation as a standard block place event
        final Block block = interaction.getClickedBlock().getRelative(interaction.getBlockFace());
        final ItemStack remaining = interaction.getPlayer().getItemInHand();
        final BlockPlaceEvent place = new BlockPlaceEvent(block, block.getState(), interaction.getClickedBlock(), remaining, interaction.getPlayer(), true);
        Bukkit.getPluginManager().callEvent(place);
        if (place.isCancelled()) return;

        // check for default owner substitute (Long names won't fit on a sign)
        final String owner = this.aliaser.alias(interaction.getPlayer().getName());
        if (owner.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH) {
            this.courier.send(interaction.getPlayer(), "requires-alias", owner, owner.length(), Locksmith.MAXIMUM_SIGN_LINE_LENGTH);
            return;
        }

        // give chance to cancel lock creation
        final BlockFace attached = interaction.getBlockFace().getOppositeFace();
        final LockCreate custom = new LockCreate(block, attached, owner, interaction.getPlayer());
        Bukkit.getPluginManager().callEvent(custom);
        if (custom.isCancelled()) return;

        // remove only 1 sign from player's hand
        if (interaction.getPlayer().getGameMode() != GameMode.CREATIVE) {
            remaining.setAmount(remaining.getAmount() - 1);
            interaction.getPlayer().setItemInHand(remaining);
        }

        this.createLock(block, attached, owner);
        interaction.setUseInteractedBlock(Result.DENY); // don't open the chest

        interaction.getPlayer().playSound(interaction.getPlayer().getLocation(), Sound.DIG_WOOD, 1.0F, 1.0F);
    }

    /** cancel block break if lock/locked and not owner */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent broken) {
        final Lock lock = this.findLock(broken.getBlock());
        if (lock == null) return;

        // Only those with lock access can break a lock
        if (lock.hasAccess(broken.getPlayer())) return;

        broken.setCancelled(true);
        final List<String> names = ConfigurationJoinListFactory.<String>create().config(this.courier.getBase()).prefix("access-").elements(lock.accessNames()).build();
        this.courier.send(broken.getPlayer(), "denied", names);
        this.logger.log(Level.FINEST, "Cancelled block break by {0} to protect lock at {1}", new Object[] { broken.getPlayer().getName(), broken.getBlock() });

        lock.sign.update();
    }

}
