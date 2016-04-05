package me.zathrasnottheone.follow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import me.zathrasnottheone.follow.Stalker;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FollowRoster {
   private static final FollowRoster instance = new FollowRoster();
   private static HashMap<String, Stalker> ROSTER = new HashMap<String, Stalker>();

   public static FollowRoster getInstance() {
      return instance;
   }

   public HashSet<Stalker> getStalkersForSuspect(Player suspect) {
      Iterator<String> iterator = ROSTER.keySet().iterator();
      HashSet<Stalker> stalkers = new HashSet<Stalker>();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(suspect.getName().equalsIgnoreCase(s.getSuspectName())) {
            stalkers.add(s);
         }
      }

      return stalkers;
   }

   public Stalker getStalker(String stalkerName) {
      Iterator<String> iterator = ROSTER.keySet().iterator();
      Stalker stalker = null;

      while(iterator.hasNext() && stalker == null) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(stalkerName.equalsIgnoreCase(s.getName())) {
            stalker = s;
         }
      }

      return stalker;
   }

   public void follow(Player stalker, Player suspect, int distance) {
      ROSTER.put(stalker.getName(), new Stalker(stalker.getName(), suspect.getName(), distance));
   }

   public Stalker unfollow(Player stalker) {
      return (Stalker)ROSTER.remove(stalker.getName());
   }

   public int getSize() {
      return ROSTER.size();
   }

   public String[] toStringArray() {
      String[] result = new String[ROSTER.size() + 2];
      Iterator<String> iterator = ROSTER.keySet().iterator();
      byte i = 0;
      int var6 = i + 1;

      Stalker value;
      for(result[i] = ChatColor.GOLD + "===== Follow List ======"; iterator.hasNext(); result[var6++] = ChatColor.GOLD + "• " + ChatColor.RED + value.getName() + ChatColor.GOLD + " is following " + ChatColor.WHITE + value.getSuspectName() + ChatColor.GOLD + " at distance " + ChatColor.AQUA + value.getDistance()) {
         String key = (String)iterator.next();
         value = (Stalker)ROSTER.get(key);
      }

      result[var6++] = ChatColor.GOLD + "===== End Of List =====";
      return result;
   }

   public void remove(Player player) {
      this.unfollow(player);
      this.removeStalkersForSuspect(player);
   }

   private void removeStalkersForSuspect(Player suspect) {
      Iterator<String> iterator = ROSTER.keySet().iterator();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(suspect.getName().equalsIgnoreCase(s.getSuspectName())) {
            ROSTER.remove(s.getName());
         }
      }

   }

   public boolean isSuspect(String suspectName) {
      Iterator<String> iterator = ROSTER.keySet().iterator();

      while(iterator.hasNext()) {
         Stalker s = (Stalker)ROSTER.get(iterator.next());
         if(suspectName == s.getSuspectName()) {
            return true;
         }
      }

      return false;
   }
}
