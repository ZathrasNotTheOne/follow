/*
 * Follow for Bukkit.
 * Copyright (C) 2012 ZathrasNotTheOne 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.zathrasnottheone.follow;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
//import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener
{
	private static final int coolDownSeconds = 1;
	private Logger logger = null;
	private Follow plugin = null;

	/* --------------------------------------------------------------------------- */
	public PlayerListener(Follow follow)
	{
		plugin = follow;
		logger = plugin.getLogger();
	}
	
	/* --------------------------------------------------------------------------- */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerMove(PlayerMoveEvent e)
	{
		FollowRoster roster = FollowRoster.getInstance();
		Player player = e.getPlayer();
		Set<Stalker> stalkers = null;
		
//		logger.info("Pitch = " + player.getLocation().getPitch());
		// get the list of stalkers for this player
//		logger.info("Looking up " + player.getName());
		stalkers = roster.getStalkersForSuspect(player);
		
		// if the player that moved is not a suspect
		if (stalkers.isEmpty())
		{
			return;
		}
		
		// this is a player that is being watched!
//		logger.info("Found suspect " + player.getName() + " in the roster.");
		
		Location from = e.getFrom();
		Location to = e.getTo();
		
		// if the distance was more than some minimum then it's a significant move
		// note that this filters out events when the suspect rotates (yaw) or looks up/down (pitch)
		Double moveDistance = from.distance(to);
		if (moveDistance > 0.2)
		{
//			logger.info("Player " + player.getName() + " moved " + moveDistance);
			updateStalkers(stalkers, player, to);
		}
	}
	
	/* --------------------------------------------------------------------------- */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerTeleport(PlayerTeleportEvent e)
	{
		FollowRoster roster = FollowRoster.getInstance();
		Player player = e.getPlayer();
		Set<Stalker> stalkers = null;
		
		// get the list of stalkers for this player
//		logger.info("Looking up " + player.getName());
		stalkers = roster.getStalkersForSuspect(player);
		
		// if the player that moved is not a suspect
		if (stalkers.isEmpty())
		{
			return;
		}
		
		// this is a player that is being watched!
//		logger.info("Found suspect " + player.getName() + " in the roster.");
		
		Location from = e.getFrom();
		Location to = e.getTo();
		
		// if the distance was more than some minimum then it's a significant move
		// note that this filters out events when the suspect rotates (yaw) or looks up/down (pitch)
		// TODO need to prevent IllegalArgumentException: Cannot measure distance between earth and survival
		Double moveDistance = from.distance(to);
		if (moveDistance > 0.2)
		{
//			logger.info("Player " + player.getName() + " teleported " + moveDistance);
			updateStalkers(stalkers, player, to);
		}
	}
	
	/* --------------------------------------------------------------------------- */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogout(PlayerQuitEvent e)
	{
		FollowRoster roster = FollowRoster.getInstance();
		Player player = e.getPlayer();

		// update the roster
		roster.remove(player);
		
	}
	
	/* --------------------------------------------------------------------------- */
	private void updateStalkers(Set<Stalker> stalkers, Player suspectPlayer, Location to)
	{
		Player stalkingPlayer = null;

		assert (stalkers != null);
		
		// go through the list of stalkers and update them 
		Iterator<Stalker> iterator = stalkers.iterator();
		while (iterator.hasNext())
		{
			Stalker s = (Stalker) iterator.next();
			stalkingPlayer = Bukkit.getPlayer(s.getName());
			
			// make sure the stalker is an active player
			if (stalkingPlayer == null)
			{
				// he went off line	so skip this stalker
				continue;
			}

			// check to ensure the stalker has cooled down before moving him again so soon
			if (!s.isCooledDown(coolDownSeconds))
			{
				continue;
			}
			
			// move the stalker - if successful, then reset the cool down period
			if (moveStalker(stalkingPlayer, suspectPlayer, s.getDistance(), to))
			{
				s.heatUp();
				
				// print a semi-random number of minutes message
				if (s.getAge() % 60 == 0)
				{
					logger.info(s.getName() + " has been following " + s.getSuspectName() + " for " + s.getAge() / 60 + " minutes.");
				}
			}
		}
		
	}

	/* --------------------------------------------------------------------------- */
	private boolean moveStalker(Player stalkingPlayer, Player suspectPlayer, int preferredDistance, Location to) 
	{
		Location stalkerLocation = stalkingPlayer.getLocation();
		Location suspectLocation = to;
		
//		int threshold = 3;
		
		// make sure we're moving to the same world or we'll get errors
		World w = suspectLocation.getWorld();
		if (!stalkerLocation.getWorld().getName().equalsIgnoreCase(w.getName()))
		{
			stalkerLocation.setWorld(w);
		}

//		logger.info("stalker yaw = " + stalkerLocation.getYaw());
//		logger.info("suspect yaw = " + suspectLocation.getYaw());
		
		// how far are we now?
		double actualDistance = stalkerLocation.distance(suspectLocation);
		double ratio = preferredDistance/actualDistance;

		// calculate delta xz
		double deltax = suspectLocation.getX()-stalkerLocation.getX();
		double deltaz = suspectLocation.getZ()-stalkerLocation.getZ();
		
		// calculate new xz moving on the line towards suspect
		double x = suspectLocation.getX()-(deltax*ratio);
		double z = suspectLocation.getZ()-(deltaz*ratio);
		
		// use the same y as the suspect
		double y = suspectLocation.getY();

		// TODO: make this xyz safe
		y = makeSafeY(w, x, y, z);
		double deltay = suspectLocation.getY()-y;
		// temporary hack
//		y = w.getHighestBlockAt((int)Math.round(x), (int)Math.round(z)).getY();
//		logger.info("y = " + y);
		
		// calculate new yaw - head rotation
		double viewDirection = 90.0;  // default value for denominator == 0.0
		if (deltaz != 0.0)
		{
			viewDirection = Math.toDegrees(Math.atan(-deltax/deltaz));
		}
		if (deltaz < 0.0)
		{
			// Quadrant II & III
			viewDirection += 180.0;
		}
		else if ((deltax > 0.0) && (deltaz > 0.0))
		{
			// Quadrant IV
			viewDirection += 360.0;
		}
		
		// debug messages
//		logger.info("actualDistance = " + actualDistance);
//		logger.info("ratio = " + ratio);
//		logger.info("deltax = " + deltax);
//		logger.info("deltaz = " + deltaz);
//		logger.info("x = " + x);
//		logger.info("z = " + z);
//		logger.info("y = " + y);
//		logger.info("viewDirection = " + viewDirection);

		// set the new location
		stalkerLocation.setX(x);
		stalkerLocation.setY(y);
		stalkerLocation.setZ(z);
		stalkerLocation.setYaw((float) viewDirection);
//		stalkerLocation.setPitch((float) 1.0);
		stalkerLocation.setPitch((float) calculatePitch(deltax, deltay, deltaz));
		
//		logger.info("Teleporting " + stalkingPlayer.getName() + 
//				" to (" + stalkerLocation.getX() + ", " + stalkerLocation.getY() + ", " + stalkerLocation.getZ() + ")" + 
//				" [" + stalkerLocation.getYaw() + ", " + stalkerLocation.getPitch() + "]");
		
		// teleport the stalker and return the boolean
		return stalkingPlayer.teleport(stalkerLocation);
	}

	/* --------------------------------------------------------------------------- */
	private double calculatePitch(double x, double y, double z) 
	{
		double pitch = 0.0;
		double a = Math.sqrt(x*x + z*z);
		double theta = Math.atan(y/a);
		pitch = -Math.toDegrees(theta);
//		logger.info("x="+x+" y="+y+" z="+z+" a="+a+" theta="+theta+" Rad ("+pitch+" Deg)");
		return pitch;
	}

	/* --------------------------------------------------------------------------- */
	private double makeSafeY(World w, double dx, double dy, double dz) 
	{
		int x = (int) Math.round(dx);
		int y = (int) Math.round(dy);
		int z = (int) Math.round(dz);
		
		// this is the return value... if anything goes wrong, return the value passed in
		Double newy = dy;
		
		// go up by twos until we have a pair of empty spaces
		while (!safe(w, x, y, z))
		{
			// go up two
			y = y+2;
		}
		
		// go down by one until we have a good block
		do
		{
			// go down one
			y = y - 1;
		}
		while (safe(w, x, y, z));
		
		// set y to the lowest empty block
		newy = y + 1.0;
		
		return newy;
	}

	/* --------------------------------------------------------------------------- */
	private boolean safe(World w, int x, int y, int z) 
	{
		return (w.getBlockAt(x, y, z).isEmpty() && w.getBlockAt(x, y+1, z).isEmpty());
	}

}
