package me.zathrasnottheone.follow;

import me.zathrasnottheone.follow.FollowConfig;
import org.bukkit.Bukkit;

public class Stalker {
   private int _maxDistance = Bukkit.getServer().getViewDistance() * 16 / 2;
   private String _name = null;
   private String _suspect = null;
   private int _distance = FollowConfig.getInstance().getFollowDistance();
   private final long beginTime = System.currentTimeMillis();
   private long lastUpdate = 0L;

   public Stalker(String name, String suspect, int distance) {
      this._name = name;
      this._suspect = suspect;
      this._distance = distance > 0?(distance < this._maxDistance?distance:this._maxDistance):1;
   }

   public String getName() {
      return this._name;
   }

   public String getSuspectName() {
      return this._suspect;
   }

   public int getDistance() {
      return this._distance;
   }

   public boolean isCooledDown(int coolDownSeconds) {
      return System.currentTimeMillis() > this.lastUpdate + (long)(coolDownSeconds * 1000);
   }

   public void heatUp() {
      this.lastUpdate = System.currentTimeMillis();
   }

   public int getAge() {
      return (int)((System.currentTimeMillis() - this.beginTime) / 1000L);
   }

   public void setDistance(int newDistance) {
      this._distance = newDistance > 0?(newDistance < this._maxDistance?newDistance:this._maxDistance):1;
   }
}
