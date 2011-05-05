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
		this.typeId = typeId;
		this.amount = amount;
		this.durability = durability;
	}

	//------------------------------------------------------------------------------------- getAmount
	public int getAmount()
	{
		return amount;
	}

	//--------------------------------------------------------------------------------- getDurability
	public short getDurability()
	{
		return durability;
	}

	//------------------------------------------------------------------------------------- getTypeId
	public int getTypeId()
	{
		return typeId;
	}

	//------------------------------------------------------------------------------- getTypeIdDamage
	public String getTypeIdDamage()
	{
		if (durability > 0) {
			return typeId + ":" + durability;
		} else {
			return "" + typeId;
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
