package com.bukkit.tickleman.RealPlugin;

//############################################################################# class RealItemStack
public class RealItemStack
{

	private int typeId;
	private int amount;
	private short durability;

	//###################################################################################### PUBLIC

	//------------------------------------------------------------------------------- RealItemStack
	public RealItemStack(int typeId, int amount, short durability)
	{
		this.typeId = typeId;
		this.amount = amount;
		this.durability = durability;
	}

	//----------------------------------------------------------------------------------- getTypeId
	public int getTypeId()
	{
		return typeId;
	}

	//----------------------------------------------------------------------------------- getAmount
	public int getAmount()
	{
		return amount;
	}

	//------------------------------------------------------------------------------- getDurability
	public short getDurability()
	{
		return durability;
	}

}
