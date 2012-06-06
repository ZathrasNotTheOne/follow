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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Follow extends JavaPlugin 
{
	private final List<Listener> listenerList = new ArrayList<Listener>();

	@Override
	public void onDisable() 
	{
		Logger logger = this.getLogger();
		
		// TODO: save stuff
		// cancel any tasks out there
		getServer().getScheduler().cancelAllTasks();	
		logger.info( "Follow disabled.");			
	} // onDisable
	
	@Override
	public void onEnable() 
	{
		Logger logger = this.getLogger();
		
		// create list of events to register
		listenerList.add(new PlayerListener(this));
		
		// register events
		for (Listener l : listenerList)
		{
			getServer().getPluginManager().registerEvents(l, this);
			logger.info("Registered listener for ".concat(l.toString()));			
		}
		
		// create the instance of the class that handles the commands from chat or console
		CommandHandler ch = new CommandHandler(this);
		getCommand("follow").setExecutor(ch);
		getCommand("unfollow").setExecutor(ch);
		logger.info("Registered commands.");			
	
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled.");			
	} // onEnable

}