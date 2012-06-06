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

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor 
{
	private Logger logger = null;
	private Follow plugin = null;

	/* --------------------------------------------------------------------------- */
	public CommandHandler(Follow follow) 
	{
		plugin = follow;
		logger = plugin.getLogger();
	}

	/* --------------------------------------------------------------------------- */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdAlias, String[] args) 
	{
		// is this the follow command?
		if (cmd.getName().equalsIgnoreCase("follow"))
		{
			if (sender.hasPermission("follow.access"))
			{
				// this is the follow command... how many arguments came with it?
				if (args.length > 0)
				{
					// with at least on argument
					if (args[0].equalsIgnoreCase("list"))
					{
						if (sender.hasPermission("follow.list"))
						{
							handleFollowList(sender, args);
							return true;
						}
						else
						{
							sender.sendMessage("You do not have list permission.");
						}
					}
					else if (args[0].equalsIgnoreCase("stop"))
					{
						handleFollowStop(sender);
						return true;
					}
					else if (args[0].equalsIgnoreCase("help"))
					{
						handleFollowHelp(sender, cmd, args);
						return true;
					}
					else // unrecognized argument
					{
						if (sender.hasPermission("follow.player"))
						{
							handleFollowPlayer(sender, cmd, args);
						}
						else // no permission to follow someone
						{
							sender.sendMessage("You do not have permission to follow another player.");
						}
					}
				}
				else // no arguments
				{
					return false;
				}
			}
			else // no follow.access permission
			{
				sender.sendMessage("You do not have access permission.");
			}
		}
		
		// not the follow command - is this the unfollow command?
		else if (cmd.getName().equalsIgnoreCase("unfollow"))
		{
			// this is the unfollow command
			handleFollowStop(sender);
		}
		else // unrecognized command
		{
			sender.sendMessage(cmd.getUsage());
		}
						
		// TODO Auto-generated method stub
		return false;
	}

	/* --------------------------------------------------------------------------- */
	private void handleFollowPlayer(CommandSender sender, Command cmd, String[] args) 
	{
		Player stalker = null;
		Player suspect = null;
		int distance = 6;
		
		// follow player with some arguments
		if (args.length == 0)
		{
			logger.warning("handleFollowPlayer() called with zero arguments.");
			return;
		}
		
		// if this command was issued by an in-game player
		if (sender instanceof Player)
		{
			// then he becomes the stalker
			stalker = (Player) sender;
		}
		
		// it was sent from an entity not in-game, probably console
		else
		{
			// tell whoever issued this command that they can't follow 
			sender.sendMessage(sender.getName() + " can not follow another player - must be in-game to follow.");
			return;
		}
		
		// if there is at least one argument sent
		if (args.length > 0)
		{
//			logger.info("handleFollowPlayer() with arguments.");

			// who does the user wish to follow?  it's in the first argument spot
//			logger.info("suspectName is " + args[0]);
			
			// look up player in the active player list
			suspect = Bukkit.getServer().getPlayer(args[0]);
			
			// if player not found on the server then there is no one to follow
			if (suspect == null)
			{
				sender.sendMessage("You didn't specify anyone to follow.");
				sender.sendMessage(cmd.getUsage());
				return;
			}
			
			// make sure player is not trying to follow own self
			if (suspect.getName().equalsIgnoreCase(sender.getName()))
			{
				sender.sendMessage("You can't follow yourself.");
				return;
			}
			
			// a player was found, so let's figure out how far away to stay while we follow them 
			else if (args.length > 1)
			{
				// get the distance from the second parameter
				try
				{
					distance = Integer.parseInt(args[0]);					
				}
				catch (NumberFormatException e)
				{
					sender.sendMessage(args[0] + " was not a parseable integer - using a default value.");
				}
			}
			
			else
			{
				// just use the initial value of the distance as it was defined
			}
		}
		
		// we should have all the parameters by now
		assert (stalker != null);
		assert (suspect != null);
		assert (distance > 0);
		FollowRoster.getInstance().follow(stalker, suspect, distance);
		sender.sendMessage("You are now following " + suspect.getName() + " at distance=" + distance);
		logger.info(stalker.getName() + " is now following " + suspect.getName() + " at distance=" + distance);
	}

	/* --------------------------------------------------------------------------- */
	private void handleFollowHelp(CommandSender sender, Command cmd, String[] args) 
	{
		// give the user some help on usage
		sender.sendMessage(cmd.getUsage());
	}

	/* --------------------------------------------------------------------------- */
	private void handleFollowStop(CommandSender sender) 
	{
		if (sender instanceof Player)
		{
			// remove this person from the follow roster
			Stalker s = FollowRoster.getInstance().unfollow((Player) sender);
			if (s != null)
			{
				sender.sendMessage("You are no longer following " + s.getSuspectName());
				logger.info(sender.getName() + " is no longer following " + s.getSuspectName());				
			}
		}
		else
		{
			// do nothing for console
			sender.sendMessage(sender.getName() + " is not following anyone.");
		}
	}

	/* --------------------------------------------------------------------------- */
	private void handleFollowList(CommandSender sender, String[] args) 
	{
		// print out the follow roster for the user
		String[] listStringArray = FollowRoster.getInstance().toStringArray();
		sender.sendMessage(listStringArray);
		logger.info("Sent " + sender.getName() + " the list of followers.");
	}

}
