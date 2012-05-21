package edgruberman.bukkit.simplelocks;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import edgruberman.bukkit.accesscontrol.Principal;

public class Lock {

    private static String title;

    private Block block = null;
    private org.bukkit.block.Sign state = null;

    /**
     * Utility interaction for managing information on existing lock.
     *
     * @param block Existing sign containing lock information.
     */
    protected Lock(final Block block) {
        this(block, null, null, null);
    }

    /**
     * Create new lock from nothing with only owner access.
     *
     * @param block Block to place lock at.
     * @param attachedFace Face adjacent to lockable block.
     * @param owner Owner of the lock.
     */
    protected Lock(final Block block, final BlockFace attachedFace, final String owner) {
        this(block, attachedFace, owner, null);
    }

    /**
     * Take existing sign, convert to lock, and attach to first lockable object behind sign.
     *
     * @param block Existing sign containing lock request.
     * @param owner Owner of the lock.
     * @param lines All four lines of the sign containing any additional names with access to lock on lines 2, 3, or 4. (Only two will be added maximum.)
     */
    protected Lock(final Block block, final String owner, final String[] lines) {
        this(block, null, owner, lines);
    }

    private Lock(final Block block, final BlockFace attachedFace, final String owner, final String[] lines) {
        this.setBlock(block);

        if (owner == null) return;

        BlockFace attachedTo = null;
        if (attachedFace == null) {
            attachedTo = this.setAttachedFace();
        } else {
            attachedTo = this.setAttachedFace(attachedFace);
        }
        if (attachedTo == null) throw new IllegalArgumentException("Unable to find lockable block.");

        this.setOwner(owner);

        if (lines != null && lines.length >= 2) {
            for (final String line : Arrays.asList(lines).subList(1, Math.min(lines.length, 4))) {
                this.addAccess(line);
            }
        }

        this.setClosed();
    }

    /**
     * Sign block associated with lock.
     *
     * @return Block with lock information.
     */
    protected Block getBlock() {
        return this.block;
    }

    /**
     * Sign block containing lock information
     *
     * @param block Block with lock information.
     */
    private void setBlock(final Block block) {
        this.block = block;
    }

    /**
     * Find nearest lockable object based on initial orientation.
     */
    private BlockFace setAttachedFace() {
        Block check = null;

        final Block sign = this.getBlock();

        final org.bukkit.material.Sign material = this.getMaterialData();

        BlockFace attachedTo = null;
        switch (material.getFacing().getOppositeFace()) {
            case NORTH:
            case EAST:
            case SOUTH:
            case WEST:
                check = sign.getRelative(material.getFacing().getOppositeFace());
                if (!check.getType().equals(Material.CHEST)) return null;
                if (Lock.getLock(check) != null) return null;
                attachedTo = material.getFacing().getOppositeFace();
                break;
            case NORTH_EAST:
                check = sign.getRelative(BlockFace.NORTH);
                if (check.getType().equals(Material.CHEST)
                        && Lock.getLock(check) == null) {
                    attachedTo = BlockFace.NORTH;
                    break;
                }
                check = sign.getRelative(BlockFace.EAST);
                if (!check.getType().equals(Material.CHEST)) return null;
                if (Lock.getLock(check) != null) return null;
                attachedTo = BlockFace.EAST;
                break;
            case SOUTH_EAST:
                check = sign.getRelative(BlockFace.SOUTH);
                if (check.getType().equals(Material.CHEST)
                        && Lock.getLock(check) == null) {
                    attachedTo = BlockFace.SOUTH;
                    break;
                }
                check = sign.getRelative(BlockFace.EAST);
                if (!check.getType().equals(Material.CHEST)) return null;
                if (Lock.getLock(check) != null) return null;
                attachedTo = BlockFace.EAST;
                break;
            case SOUTH_WEST:
                check = sign.getRelative(BlockFace.SOUTH);
                if (check.getType().equals(Material.CHEST)
                        && Lock.getLock(check) == null) {
                    attachedTo = BlockFace.SOUTH;
                    break;
                }
                check = sign.getRelative(BlockFace.WEST);
                if (!check.getType().equals(Material.CHEST)) return null;
                if (Lock.getLock(check) != null) return null;
                attachedTo = BlockFace.WEST;
                break;
            case NORTH_WEST:
                check = sign.getRelative(BlockFace.NORTH);
                if (check.getType().equals(Material.CHEST)
                        && Lock.getLock(check) == null) {
                    attachedTo = BlockFace.NORTH;
                    break;
                }
                check = sign.getRelative(BlockFace.WEST);
                if (!check.getType().equals(Material.CHEST)) return null;
                if (Lock.getLock(check) != null) return null;
                attachedTo = BlockFace.WEST;
                break;
        }

        return this.setAttachedFace(attachedTo);
    }

    private BlockFace setAttachedFace(final BlockFace attachedFace) {
        if (attachedFace == null) return null;

        final Block locked = this.getBlock().getRelative(attachedFace);
        if (Lock.getLock(locked) != null) throw new IllegalArgumentException("Block already locked.");

        final org.bukkit.material.Sign material = new org.bukkit.material.Sign(Material.WALL_SIGN);
        material.setFacingDirection(attachedFace.getOppositeFace());
        this.getBlock().setTypeIdAndData(material.getItemTypeId(), material.getData(), true);

        return material.getAttachedFace();
    }

    protected boolean setOwner(String owner) {
        final Principal principal = Main.security.getAccount(owner);
        if (principal == null) return false;

        owner = Main.security.formatName(principal);
        if (owner.length() > 15) return false;

        this.getState().setLine(1, owner);
        return this.getState().update();
    }

    protected boolean addAccess(String name) {
        final Principal principal = Main.security.getAccount(name);
        if (principal == null) return false;

        name = Main.security.formatName(principal);
        if (name.length() > 15) return false;

        // Do not add access if name already has direct access. (not in a group)
        if (this.hasAccess(name, true)) return false;

        // Find first free line to add access to.
        Integer addTo = null;
        if (this.getState().getLine(2).length() == 0) addTo = 2;
        else if (this.getState().getLine(3).length() == 0) addTo = 3;

        // Do not add access if no more room on sign.
        if (addTo == null) return false;

        this.getState().setLine(addTo, name);
        return this.getState().update();
    }

    protected void removeAccess(final String name) {
        // Do not remove access if name does not already have access
        if (!this.hasAccess(name, true)) return;

        // Find line with matching name
        Integer removeFrom = null;
        if (this.getState().getLine(2) != null && this.getState().getLine(2).equalsIgnoreCase(name)) removeFrom = 2;
        else if (this.getState().getLine(3) != null && this.getState().getLine(3).equalsIgnoreCase(name)) removeFrom = 3;

        if (removeFrom == null) return;

        this.getState().setLine(removeFrom, "");
        this.getState().update();
    }

    /**
     * Force clients to render sign again to recognize new or removed text.
     * TODO schedule task to run shortly after block replaced event finishes to update block
     */
    protected void refresh() {
        // Toggle attached direction
        final org.bukkit.material.Sign material = new org.bukkit.material.Sign(this.getBlock().getType());
        material.setData(this.getMaterialData().getData());

        material.setFacingDirection(material.getAttachedFace());
        this.getBlock().setData(material.getData());

        material.setFacingDirection(material.getAttachedFace());
        this.getBlock().setData(material.getData());
    }

    private void setClosed() {
        this.getState().setLine(0, Lock.title);
        this.getState().update();
    }

    /**
     * This function exists because raising exceptions confuses me.
     *
     * @return true if this lock object was successful at creating a lock in the world.
     */
    protected boolean isLock() {
        return Lock.isLock(this.getBlock());
    }

    protected boolean hasAccess(final Player player) {
        return this.hasAccess(player.getName());
    }

    protected boolean hasAccess(final String name) {
        return this.hasAccess(name, false);
    }

    protected boolean hasAccess(final String name, final boolean isDirect) {
        if (this.isOwner(name, isDirect)) return true;

        final List<String> access = Arrays.asList(this.getState().getLines())
            .subList(2, Math.min(this.getState().getLines().length, 4));

        for (String line : access) {
            line = line.trim();

            // Direct name match.
            if (line.equalsIgnoreCase(name)) return true;

            // Check group membership.
            if (!isDirect && Main.security.memberOf(name, line)) return true;
        }

        return false;
    }

    protected String getOwner() {
        return this.getState().getLine(1);
    }

    protected boolean isOwner(final Player player) {
        return this.isOwner(player.getName());
    }

    protected boolean isOwner(final String name) {
        return this.isOwner(name, false);
    }

    protected boolean isOwner(final String name, final boolean isDirect) {
        final String owner = this.getState().getLine(1).trim();

        // Direct name match.
        if (owner.equalsIgnoreCase(name)) return true;

        // Check group membership.
        if (!isDirect && Main.security.memberOf(name, owner)) return true;

        return false;
    }

    protected String getDescription() {
        final Block locked = this.getBlock().getRelative(this.getMaterialData().getAttachedFace());

        String description = "---- Lock";
        description += "\nOwner: " + this.getOwner();
        description += "\nAccess: " + this.getState().getLine(2) + " " + this.getState().getLine(3);
        description += "\n" + locked.getType().toString() + " at x: " + locked.getX() + " y: " + locked.getY() + " z: " + locked.getZ();

        return description;
    }

    /**
     * Utility function to access sign text easier without causing NPE
     *
     * @return the sign's block state
     */
    protected org.bukkit.block.Sign getState() {
        if (this.getBlock() == null) return null;

        if (this.state == null) this.state = (org.bukkit.block.Sign) this.getBlock().getState();
        return this.state;
    }

    /**
     * Utility function to manage direction of sign easier.
     *
     * @return the sign's material data
     */
    private org.bukkit.material.Sign getMaterialData() {
        if (this.getBlock() == null) return null;

        final org.bukkit.material.Sign material = new org.bukkit.material.Sign(this.getBlock().getType());
        material.setData(this.getBlock().getData());
        return material;
    }

    /**
     * Configure the text that is required on the first line of the lock sign.
     *
     * @param title Text that designates a lock.
     */
    protected static void setTitle(final String title) {
        if (title == null || title.length() > 15)
            throw new IllegalArgumentException("Title must be no more than 15 characters.");

        Lock.title = title;
    }

    /**
     * The text on the first line of the sign that indicates it is a lock.
     *
     * @return Text that designates a lock.
     */
    protected static String getTitle() {
        return Lock.title;
    }

    /**
     * Find lock for block, which could be the block itself.
     *
     * @param block Block to check for lock on.
     * @return Lock associated with block.
     */
    protected static Lock getLock(final Block block) {
        if (Lock.isLock(block)) return new Lock(block);

        return Lock.getChestLock(block);
    }

    /**
     * Indicates if block is a lock for any object.
     *
     * @param block Block to check if it is a lock.
     * @return true if block is a lock.
     */
    protected static boolean isLock(final Block block) {
        return Lock.isLock(block, null);
    }

    /**
     * Indicates if block is a lock.
     *
     * @param block Block to check if it is a lock.
     * @param attachedTo Face connected to locked object. Set to null to determine if block is a lock for anything.
     * @return true if block is a lock attached as specified.
     */
    private static boolean isLock(final Block block, final BlockFace attachedTo) {
        // Locks are always wall signs.
        if (!block.getType().equals(Material.WALL_SIGN))
            return false;

        final org.bukkit.material.Sign material = new org.bukkit.material.Sign(block.getType());
        if (attachedTo != null) {
            // Locks must be directly attached to locked object.
            material.setData(block.getData());
            if (!material.getAttachedFace().equals(attachedTo))
                return false;
        }

        // First line of sign must contain standard lock title.
        if (!((org.bukkit.block.Sign) block.getState()).getLine(0).equals(Lock.title))
            return false;

        return true;
    }

    /**
     * Returns the lock associated with the chest.
     *
     * @param chest Chest block (single or either side of a double).
     * @return Lock associated to chest.
     */
    private static Lock getChestLock(final Block chest) {
        return Lock.getChestLock(chest, false);
    }

    /**
     * Returns the lock associated with the chest.
     *
     * @param chest Chest block to check for associated lock.
     * @param otherHalf true if chest is the second half of a chest.
     * @return Lock associated to chest.
     */
    private static Lock getChestLock(final Block chest, final boolean otherHalf) {
        if (!chest.getType().equals(Material.CHEST)) return null;

        Block block;

        block = chest.getRelative(BlockFace.NORTH);
        if (Lock.isLock(block, BlockFace.NORTH.getOppositeFace())) return new Lock(block);

        block = chest.getRelative(BlockFace.EAST);
        if (Lock.isLock(block, BlockFace.EAST.getOppositeFace())) return new Lock(block);

        block = chest.getRelative(BlockFace.SOUTH);
        if (Lock.isLock(block, BlockFace.SOUTH.getOppositeFace())) return new Lock(block);

        block = chest.getRelative(BlockFace.WEST);
        if (Lock.isLock(block, BlockFace.WEST.getOppositeFace())) return new Lock(block);

        // If this is the second half of a chest don't look further.
        if (otherHalf) return null;

        // Check if second half of chest exists and has a lock.
        block = chest.getRelative(BlockFace.NORTH);
        if (block.getType().equals(Material.CHEST))
            return Lock.getChestLock(block, true);

        block = chest.getRelative(BlockFace.EAST);
        if (block.getType().equals(Material.CHEST))
            return Lock.getChestLock(block, true);

        block = chest.getRelative(BlockFace.SOUTH);
        if (block.getType().equals(Material.CHEST))
            return Lock.getChestLock(block, true);

        block = chest.getRelative(BlockFace.WEST);
        if (block.getType().equals(Material.CHEST))
            return Lock.getChestLock(block, true);

        return null;
    }
}