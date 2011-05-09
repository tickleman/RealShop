package fr.crafter.tickleman.RealPlugin;

//############################################################################# class RealItemStack
public class RealItemStack
{

	private int amount;

	private short durability;

	private int typeId;

	//######################################################################################## PUBLIC

	//--------------------------------------------------------------------------------- RealItemStack
	public RealItemStack(int typeId, int amount, short durability)
	{
		setTypeId(typeId);
		setAmount(amount);
		setDurability(durability);
	}

	//------------------------------------------------------------------------------------- getAmount
	public int getAmount()
	{
		return amount;
	}

	//------------------------------------------------------------------------------------- getDamage
	/**
	 * get the damage value of an item stack
	 * 0   = "not damaged"
	 * 132 = "totally wreck"
	 */
	public short getDamage()
	{
		if (!typeIdHasDamage(getTypeId())) {
			// for those items, durability is a "variant" code, and not a damage value
			return 0;
		} else {
			// calculate damage for all other items
			return getDurability();
		}
	}

	//--------------------------------------------------------------------------------- getDurability
	/**
	 * Get "raw" durability code (damage or item variant, depending on item's typeId)
	 * 
	 * Durability IS NOT damage for all items typeId,
	 * so please prefer using getDamage() if you want only the damage value
	 */
	public short getDurability()
	{
		return durability;
	}

	//------------------------------------------------------------------------------------- getTypeId
	public int getTypeId()
	{
		return typeId;
	}

	//----------------------------------------------------------------- typeIdDurabilityWithoutDamage
	/**
	 * Return the typeIdDurability identifier without its damage value
	 *
	 * For some typeId, the durability is not a damage value but a variant value (wool, slabs, ...),
	 * for those, the trailing ":durability" is kept !
	 * For all others typeId, the durability is the damage value, and only the typeId is returned.
	 *
	 * typeIdDurabilityWithoutDamage("270")    = "270"  (wooden pickaxe)
	 * typeIdDurabilityWithoutDamage("270:46") = "270"  (damaged wooden pickaxe)
	 * typeIdDurabilityWithoutDamage("35:1")   = "35:1" (orange wool)
	 */
	public static String typeIdDurabilityWithoutDamage(String typeIdDurability)
	{
		if (!typeIdDurability.contains(":")) {
			return typeIdDurability;
		} else {
			if (!typeIdHasDamage(Integer.parseInt(typeIdDurability.split(":")[0]))) {
				return typeIdDurability;
			} else {
				return typeIdDurability.split(":")[0];
			}
		}
	}

	//------------------------------------------------------------------------------- typeIdHasDamage
	public static boolean typeIdHasDamage(int typeId)
	{
		return
			// those codes never have damage : durability is an item variant
			!( (typeId == 17 ) // wood
			|| (typeId == 18 ) // leaves
			|| (typeId == 35 ) // wool
			|| (typeId == 43 ) // double slab
			|| (typeId == 44 ) // slab
			|| (typeId == 263) // coal
			|| (typeId == 351) // dye
			|| (typeId == 352) // bone
		);
	}

	//------------------------------------------------------------------------------- typeIdMaxDamage
	public static int typeIdMaxDamage(int typeId)
	{
		switch (typeId) {
			case 270: return 60;
			case 273: return 132;
			// TODO : each "damageable" item may have it's one maxDamage value. Get it !
			default:  return 100;
		}
	}

	//--------------------------------------------------------------------------- getTypeIdDurability
	public String getTypeIdDurability()
	{
		if (getDamage() > 0) {
			return getTypeId() + ":" + getDamage();
		} else {
			return "" + getTypeId();
		}
	}

	//-------------------------------------------------------------- getTypeIdDurabilityWithoutDamage
	public String getTypeIdDurabilityWithoutDamage()
	{
		return typeIdDurabilityWithoutDamage(getTypeIdDurability());
	}

	//------------------------------------------------------------------------------ getTypeIdVariant
	public String getTypeIdVariant()
	{
		if ((getDamage() > 0) && !typeIdHasDamage(getTypeId())) {
			return getTypeId() + ":" + getDurability();
		} else {
			return "" + getTypeId();
		}
	}

	//------------------------------------------------------------------------------------- setAmount
	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	//--------------------------------------------------------------------------------- setDurability
	public void setDurability(short durability)
	{
		this.durability = durability;
	}

	//------------------------------------------------------------------------------------- setTypeId
	public void setTypeId(int typeId)
	{
		this.typeId = typeId;
	}

}
