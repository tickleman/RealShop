package com.bukkit.tickleman.RealShop;

//####################################################################################### RealPrice
public class RealPrice
{

	public double buy = 0;
	public double sell = 0;

	//------------------------------------------------------------------------------------- RealPrice
	public RealPrice()
	{
	}

	//------------------------------------------------------------------------------------- RealPrice
	public RealPrice(double buy, double sell)
	{
		this.buy = buy;
		this.sell = sell;
	}

	//---------------------------------------------------------------------------------------- getBuy
	public double getBuy()
	{
		return getBuy(1);
	}

	//---------------------------------------------------------------------------------------- getBuy
	public double getBuy(int quantity)
	{
		return Math.floor((double)100 * buy * (double)quantity) / (double)100;
	}

	//--------------------------------------------------------------------------------------- getSell
	public double getSell()
	{
		return getSell(1);
	}

	//--------------------------------------------------------------------------------------- getSell
	public double getSell(int quantity)
	{
		return Math.ceil((double)100 * sell * (double)quantity) / (double)100;
	}

}
