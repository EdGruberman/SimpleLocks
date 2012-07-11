package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;

class Locksmith implements Listener {

    static int MAXIMUM_SIGN_LINE_LENGTH = 15;

    Plugin plugin;
    ConfigurationSection defaultOwners = new MemoryConfiguration();
    private String title = null;

    Locksmith(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * The text on the first line of the sign that indicates it is a lock
     *
     * @return text that designates a lock
     */
    String getTitle() {
        return this.title;
    }

    /**
     * Configure the text that is required on the first line of the lock sign
     *
     * @param title text that designates a lock
     */
    void setTitle(final String title) {
        if (title.length() < 1 || title.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH)
            throw new IllegalArgumentException("Title must be between 1 and " + Locksmith.MAXIMUM_SIGN_LINE_LENGTH + " characters");

        this.title = title;
    }

    /**
     * Create new lock
     *
     * @param block where to create sign containing lock information
     * @param attached face adjacent to lockable
     * @param owner player name or group name
     */
    Lock createLock(final Block block, final BlockFace attached, final String owner) {
        return new Lock(this, block, attached, owner);
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
    Lock findLock(final Block block) {
        if (this.isLock(block)) return new Lock(this, block);

        final Block lock = this.findChestLock(block);
        if (lock == null) return null;

        return new Lock(this, lock);
    }

    /**
     * Returns the lock associated with the chest
     *
     * @param chest block (small or either side of a large)
     * @return Block containing lock associated to chest; null if none
     */
    private Block findChestLock(final Block chest) {
        if (chest.getTypeId() != Material.CHEST.getId()) return null;

        final List<BlockFace> cardinals = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

        // Check directly adjacent blocks for lock
        for (final BlockFace direction : cardinals) {
            final Block relative = chest.getRelative(direction);
            if (this.isLock(relative, direction.getOppositeFace())) return relative;
        }

        // Check directly adjacent blocks for second half of double chest
        for (final BlockFace direction : cardinals) {
            final Block relative = chest.getRelative(direction);
            if (relative.getTypeId() == Material.CHEST.getId()) {

                // Found double chest - Check directly adjacent blocks to second half of chest for lock
                for (final BlockFace direction2 : cardinals) {
                    final Block relative2 = relative.getRelative(direction2);
                    if (this.isLock(relative2, direction2.getOppositeFace())) return relative2;
                }

            }
        }

        return null;
    }

    String getDefaultOwner(final Player player) {
        return this.defaultOwners.getString(player.getName(), player.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        final Lock lock = this.findLock(event.getClickedBlock());
        if (lock != null) {
            // Existing lock found

            if (!lock.hasAccess(event.getPlayer())) {
                // Player does not have access, cancel interaction and notify player
                event.setCancelled(true);
                Main.messageManager.send(event.getPlayer(), "You do &cnot have access&_ to this lock", MessageLevel.RIGHTS, false);
                this.plugin.getLogger().finer(
                        "Lock access denied to " + event.getPlayer().getName() + " at "
                            + " x:" + event.getClickedBlock().getX()
                            + " y:" + event.getClickedBlock().getY()
                            + " z:" + event.getClickedBlock().getZ()
                );
                return;
            }

            // Player has access and they right clicked on the lock itself so give them information
            if (this.isLock(event.getClickedBlock()))
                Main.messageManager.send(event.getPlayer(), "You &ahave access&_ to this lock." + (lock.isOwner(event.getPlayer()) ? " &dTo modify: /lock (+|-) <Player>" : ""), MessageLevel.STATUS, false);

            return;
        }

        // No existing lock, check to see if player is requesting a lock be created
        if (event.getClickedBlock().getType().equals(Material.CHEST)) {
            if (event.getMaterial().equals(Material.SIGN)
                    && event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.AIR)
                    && !event.getBlockFace().equals(BlockFace.UP) && !event.getBlockFace().equals(BlockFace.DOWN)) {

                // Right click on a chest with a sign to create lock automatically
                event.setUseInteractedBlock(Result.DENY); // Don't open the chest

                // Check for default owner substitute (Long names won't fit on a sign)
                final String ownerName = this.getDefaultOwner(event.getPlayer());
                if (ownerName.length() > 15) {
                    Main.messageManager.send(event.getPlayer(), "Unable to create lock; Owner name is too long", MessageLevel.SEVERE, false);
                    return;
                }

                event.getPlayer().setItemInHand(null);
                this.createLock(event.getClickedBlock().getRelative(event.getBlockFace()), event.getBlockFace().getOppositeFace(), ownerName);
            }
        }
    }

    /**
     * Cancel block break if lock/locked and not owner
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        final Lock lock = this.findLock(event.getBlock());
        if (lock == null) return;

        // Allow lock owner to break lock
        if (lock.isOwner(event.getPlayer())) return;

        event.setCancelled(true);
        Main.messageManager.send(event.getPlayer(), "You can not remove a lock unless you are the owner", MessageLevel.RIGHTS, false);
        this.plugin.getLogger().finer(
                "Cancelled block break to protect lock at"
                    + " x:" + event.getBlock().getX()
                    + " y:" + event.getBlock().getY()
                    + " z:" + event.getBlock().getZ()
        );

        lock.refresh(); // TODO add timed refresh so it updates after event processes/reverts
    }

    /**
     * Cancel explosion if any affected block is a lock or is locked
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        for (final Block block : event.blockList()) {
            if (this.isLock(block) || this.isLocked(block)) {
                event.setCancelled(true);
                this.plugin.getLogger().finer("Cancelling explosion to protect lock at" + " x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
                break;
            }
        }
    }

}
