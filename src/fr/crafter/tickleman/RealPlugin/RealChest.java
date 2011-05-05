package fr.crafter.tickleman.RealPlugin;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

//####################################################################################### RealChest
/*
 * This Chest object manages small and big chests
 */
public class RealChest
{

	/**
	 * Main chest object
	 * - always set by constructor 
	 */
	private final Chest mainChest;

	private String mainChestId;

	/**
	 * Neighbor chest object
	 * - null if block is a small chest
	 * - another Chest object for the secondary chest when two adjacent blocks contain a big Chest
	 */
	private final Chest neighborChest;

	private String neighborChestId;

	//------------------------------------------------------------------------------------- ReadChest
	/**
	 * Constructor #1 : create chest from an existing block reference
	 * - block must be a chest tested with block.getType().equals(Material.CHEST) !
	 */
	private RealChest(Block block)
	{
		mainChest = (Chest)block.getState();
		Block neighborBlock = scanForNeighborChest(
			block.getWorld(), block.getX(), block.getY(), block.getZ()
		);
		if (neighborBlock == null) {
			neighborChest = null;
		} else {
			neighborChest = (Chest)neighborBlock.getState();
		}
	}

	//------------------------------------------------------------------------------------- RealChest
	/**
	 * Constructor #2 : create chest from an existing word and coordinates
	 * - block at coordinates must be a chest tested with 
	 *   world.getBlockAt(x - 1, y, z)).getType().equals(Material.CHEST)
	 */
	private RealChest(World world, int x, int y, int z)
	{
		mainChest = (Chest)world.getBlockAt(x, y, z).getState();
		mainChestId = world.getName() + "," + x + "," + y + "," + z;
		Block neighborBlock = scanForNeighborChest(world, x, y, z);
		if (neighborBlock == null) {
			neighborChest = null;
			neighborChestId = "";
		} else {
			neighborChest = (Chest)neighborBlock.getState();
			neighborChestId = world.getName() + ","
			+ neighborBlock.getX() + "," + neighborBlock.getY() + "," + neighborBlock.getZ();
		}
	}

	//######################################################################################## PUBLIC

	//---------------------------------------------------------------------------------------- create
	/**
	 * Static constructor #1
	 * - returns null if block is not a chest, or a pointer to RealChest 
	 */
	public static RealChest create(Block block)
	{
		if (block.getType().equals(Material.CHEST)) {
			return new RealChest(block.getWorld(), block.getX(), block.getY(), block.getZ());
		} else {
			return null;
		}
	}

	//---------------------------------------------------------------------------------------- create
	/**
	 * Static constructor #2
	 * - returns null if block is not a chest, or a pointer to RealChest 
	 */
	public static RealChest create(World world, int x, int y, int z)
	{
		if (world.getBlockAt(x, y, z).getType().equals(Material.CHEST)) {
			return new RealChest(world, x, y, z);
		} else {
			return null;
		}
	}

	//---------------------------------------------------------------------------------------- fullId
	public String getChestId()
	{
		if (mainChestId.compareTo(neighborChestId) < 0) {
			return mainChestId + "-" + neighborChestId;
		} else {
			return neighborChestId + "-" + mainChestId;
		}
	}

	//---------------------------------------------------------------------------------- getMainChest
	public Chest getMainChest()
	{
		return mainChest;
	}

	//------------------------------------------------------------------------------ getNeighborChest
	public Chest getNeighborChest()
	{
		return neighborChest;
	}

	//-------------------------------------------------------------------------- scanForNeighborChest
	/**
	 * Return foreign chest block for big chests, or null if no foreign chest was found
	 * This is for internal use, but could be useful to any other mod
	 */
	public static Block scanForNeighborChest(World world, int x, int y, int z)
	{
		Block block;
		if ((block = world.getBlockAt(x - 1, y, z)).getType().equals(Material.CHEST)) {
			return block;
		}
		if ((block = world.getBlockAt(x + 1, y, z)).getType().equals(Material.CHEST)) {
			return block;
		}
		if ((block = world.getBlockAt(x, y, z - 1)).getType().equals(Material.CHEST)) {
			return block;
		}
		if ((block = world.getBlockAt(x, y, z + 1)).getType().equals(Material.CHEST)) {
			return block;
		}
		return null;
	}

	//-------------------------------------------------------------------------- scanForNeighborChest
	public static Block scanForNeighborChest(Block block)
	{
		return scanForNeighborChest(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}

}
