package me.zathrasnottheone.follow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import me.zathrasnottheone.follow.CommandHandler;
import me.zathrasnottheone.follow.FollowConfig;
import me.zathrasnottheone.follow.MetricsLite;
import me.zathrasnottheone.follow.PlayerListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Follow extends JavaPlugin {
   private final List<Listener> _listenerList = new ArrayList<Listener>();

   public void onDisable() {
      Logger logger = this.getLogger();
      this.saveConfig();
      this.getServer().getScheduler().cancelAllTasks();
      logger.info("Disabled.");
   }

   public void onEnable() {
      Logger logger = this.getLogger();
      FollowConfig.getInstance().initiatlize(this);
      this._listenerList.add(new PlayerListener(this));
      Iterator<Listener> pdfFile = this._listenerList.iterator();

      while(pdfFile.hasNext()) {
         Listener ch = (Listener)pdfFile.next();
         this.getServer().getPluginManager().registerEvents(ch, this);
      }

      CommandHandler ch1 = new CommandHandler(this);
      this.getCommand("follow").setExecutor(ch1);
      this.getCommand("unfollow").setExecutor(ch1);

      try {
         MetricsLite pdfFile1 = new MetricsLite(this);
         pdfFile1.start();
      } catch (IOException var4) {
         ;
      }

      PluginDescriptionFile pdfFile2 = this.getDescription();
      logger.info(pdfFile2.getName() + " version " + pdfFile2.getVersion() + " is enabled.");
   }
}
