package me.zathrasnottheone.follow;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.entity.Player;

public class FollowRoster 
{
	private static final FollowRoster instance = new FollowRoster();
	private static HashMap<String, Stalker> ROSTER = new HashMap<String, Stalker>();

	/* --------------------------------------------------------------------------- */
	private FollowRoster()
	{
		// not allowed
	}
	
	/* --------------------------------------------------------------------------- */
	public static FollowRoster getInstance()
	{
		return instance;
	}

	/* --------------------------------------------------------------------------- */
	public Set<Stalker> getStalkersForSuspect(Player suspect) 
	{
		Iterator<String> iterator = ROSTER.keySet().iterator();
		Set<Stalker> stalkers = new HashSet<Stalker>();
		
		while (iterator.hasNext())
		{
			Stalker s = ROSTER.get(iterator.next());
			
			// if the player passed in is found in our roster
			if (suspect.getName().equalsIgnoreCase(s.getSuspectName()))
			{
				// add to the set of stalkers to return
				stalkers.add(s);
			}
		}
		return stalkers;
	}
	
	/* --------------------------------------------------------------------------- */
	public void follow(Player stalker, Player suspect, int distance)
	{
		ROSTER.put(stalker.getName(), new Stalker(stalker.getName(), suspect.getName(), distance));
	}
	
	/* --------------------------------------------------------------------------- */
	public Stalker unfollow(Player stalker)
	{
		return ROSTER.remove(stalker.getName());
	}
	
	/* --------------------------------------------------------------------------- */
	public int getSize()
	{
		return ROSTER.size();
	}
	
	/* --------------------------------------------------------------------------- */
	public String[] toStringArray()
	{
		String[] result = new String[ROSTER.size()];
		Iterator<String> iterator = ROSTER.keySet().iterator();
		int i = 0;
		while (iterator.hasNext())
		{
			String key = iterator.next();
			Stalker value = ROSTER.get(key);
			result[i++] = new String(value.getName() + " is following " + value.getSuspectName() + " at distance=" + value.getDistance());
		}
		return result;
	}

	/* --------------------------------------------------------------------------- */
	public void remove(Player player) 
	{
		// if a stalker, remove him as a key
		unfollow(player);
		
		// if a suspect, remove all his stalkers (suspect can have more than one stalker)
		removeStalkersForSuspect(player);
	}

	/* --------------------------------------------------------------------------- */
	private void removeStalkersForSuspect(Player suspect) 
	{
		Iterator<String> iterator = ROSTER.keySet().iterator();
		
		while (iterator.hasNext())
		{
			Stalker s = ROSTER.get(iterator.next());
			
			// if the player passed in is found in our roster
			if (suspect.getName().equalsIgnoreCase(s.getSuspectName()))
			{
				// get this stalker/suspect pair out of the roster
				ROSTER.remove(s.getName());
			}
		}
	}
}
