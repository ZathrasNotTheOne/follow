package me.zathrasnottheone.follow;

public class Stalker 
{
	private String _name = null;
	private String _suspect = null;
	private int _distance = 7;
	private final long beginTime = System.currentTimeMillis();
	private long lastUpdate = 0;
	
	/* --------------------------------------------------------------------------- */
	public Stalker(String name, String suspect, int distance)
	{
		_name = name;
		_suspect = suspect;
		_distance = distance;
	}

	/* --------------------------------------------------------------------------- */
	public String getName() 
	{
		return _name;
	}

	/* --------------------------------------------------------------------------- */
	public String getSuspectName() 
	{
		return _suspect;
	}

	/* --------------------------------------------------------------------------- */
	public int getDistance() 
	{
		return _distance;
	}

	/* --------------------------------------------------------------------------- */
	public boolean isCooledDown(int coolDownSeconds)
	{
		return (System.currentTimeMillis() > (lastUpdate+ (coolDownSeconds * 1000)));
	}
	
	/* --------------------------------------------------------------------------- */
	public void heatUp()
	{
		lastUpdate = System.currentTimeMillis();
	}
	
	/* --------------------------------------------------------------------------- */
	public int getAge()
	{
		return (int) ((System.currentTimeMillis() - beginTime) / 1000);
	}
}
