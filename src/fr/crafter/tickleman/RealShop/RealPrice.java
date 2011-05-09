package fr.crafter.tickleman.RealShop;

//####################################################################################### RealPrice
public class RealPrice
{

	public double buy = 0;
	public double sell = 0;
	public Double damagedBuy = null;
	public Double damagedSell = null;

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
		if (damagedBuy != null) {
			if (quantity <= 1) {
				return Math.floor((double)100 * damagedBuy * (double)quantity) / (double)100;
			} else {
				// damaged quantity only on first item of the stack
				return damagedBuy + Math.floor((double)100 * buy * (double)(quantity - 1)) / (double)100;
			}
		} else {
			return Math.floor((double)100 * buy * (double)quantity) / (double)100;
		}
	}

	//--------------------------------------------------------------------------------------- getSell
	public double getSell()
	{
		return getSell(1);
	}

	//--------------------------------------------------------------------------------------- getSell
	public double getSell(int quantity)
	{
		if (damagedSell != null) {
			if (quantity <= 1) {
				return Math.ceil((double)100 * damagedSell * (double)quantity) / (double)100;
			} else {
				return damagedSell + Math.ceil((double)100 * sell * (double)(quantity - 1)) / (double)100;
			}
		}
		return Math.ceil((double)100 * sell * (double)quantity) / (double)100;
	}

}
