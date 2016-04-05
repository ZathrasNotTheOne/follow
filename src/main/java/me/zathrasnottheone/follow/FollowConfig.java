package me.zathrasnottheone.follow;

import java.util.logging.Logger;
import me.zathrasnottheone.follow.Follow;
import org.bukkit.configuration.file.FileConfiguration;

public class FollowConfig {
   private static final FollowConfig _instance = new FollowConfig();
   public final int _defaultFollowDistance = 8;
   public final int _defaultCoolDown = 3;
   public final double _defaultSignificantDistance = 0.21D;
   public final boolean _defaultRotateHead = false;
   private Follow _plugin = null;
   private Logger _logger = null;
   private FileConfiguration _config = null;

   public static FollowConfig getInstance() {
      return _instance;
   }

   public void initiatlize(Follow follow) {
      this._plugin = follow;
      this._logger = this._plugin.getLogger();
      this._config = this._plugin.getConfig();
      this._plugin.getConfig().options().copyDefaults(true);
      this._plugin.saveConfig();
      this._logger.info("Configuration initialized.");
   }

   public int getFollowDistance() {
      return this._config.getInt("followDistance", 8);
   }

   public int getCoolDown() {
      return this._config.getInt("coolDown", 3);
   }

   public double getSignificantDistance() {
      return this._config.getDouble("significantDistance", 0.21D);
   }

   public boolean isRotateHead() {
      return this._config.getBoolean("rotateHead", false);
   }

   public void saveConfig() {
      this._plugin.saveConfig();
   }

   public void reloadConfig() {
      this._plugin.reloadConfig();
   }

}
