package me.zathrasnottheone.follow;

import java.util.ArrayList;
import java.util.logging.Logger;
import me.zathrasnottheone.follow.Follow;
import me.zathrasnottheone.follow.FollowConfig;
import me.zathrasnottheone.follow.FollowRoster;
import me.zathrasnottheone.follow.Stalker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
   private Logger logger = null;
   private Follow plugin = null;

   public CommandHandler(Follow follow) {
      this.plugin = follow;
      this.logger = this.plugin.getLogger();
   }

   public boolean onCommand(CommandSender sender, Command cmd, String cmdAlias, String[] args) {
      if(cmd.getName().equalsIgnoreCase("follow")) {
         if(sender.hasPermission("follow.access")) {
            if(args.length > 0) {
               if(args[0].equalsIgnoreCase("list")) {
                  if(sender.hasPermission("follow.list")) {
                     this.handleFollowList(sender, args);
                  } else {
                     sender.sendMessage(ChatColor.GOLD + "You do not have list permission.");
                  }
               } else if(args[0].equalsIgnoreCase("reload")) {
                  if(sender.hasPermission("follow.reload")) {
                     this.handleReload(sender);
                  } else {
                     sender.sendMessage(ChatColor.GOLD + "You do not have list permission.");
                  }
               } else if(args[0].equalsIgnoreCase("closer")) {
                  if(sender.hasPermission("follow.player")) {
                     this.handleMoveCloser(sender);
                  } else {
                     sender.sendMessage(ChatColor.GOLD + "You do not have permission to follow another player.");
                  }
               } else if(args[0].equalsIgnoreCase("farther")) {
                  if(sender.hasPermission("follow.player")) {
                     this.handleMoveFarther(sender);
                  } else {
                     sender.sendMessage(ChatColor.GOLD + "You do not have permission to follow another player.");
                  }
               } else if(args[0].equalsIgnoreCase("stop")) {
                  this.handleFollowStop(sender);
               } else if(args[0].equalsIgnoreCase("help")) {
                  this.handleFollowHelp(sender, cmd, args);
               } else if(sender.hasPermission("follow.player")) {
                  this.handleFollowPlayer(sender, cmd, args);
               } else {
                  sender.sendMessage(ChatColor.GOLD + "You do not have permission to follow another player.");
               }
            } else if(sender.hasPermission("follow.player")) {
               this.handleFollowPlayer(sender, cmd, args);
            } else {
               sender.sendMessage(ChatColor.GOLD + "You do not have permission to follow another player.");
            }
         } else {
            sender.sendMessage(ChatColor.GOLD + "You do not have access permission.");
         }
      } else if(cmd.getName().equalsIgnoreCase("unfollow")) {
         this.handleFollowStop(sender);
      } else {
         sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
      }

      return true;
   }

   private void handleMoveCloser(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker stalker = FollowRoster.getInstance().getStalker(sender.getName());
         if(stalker != null) {
            stalker.setDistance((stalker.getDistance() - 1) / 2 + 1);
            sender.sendMessage(ChatColor.RED + "You" + ChatColor.GOLD + " are now following at distance " + ChatColor.AQUA + stalker.getDistance());
         } else {
            this.logger.severe("Sender " + sender.getName() + " was not in the stalker list.");
            sender.sendMessage(ChatColor.RED + "Couldn\'t find you in the stalker list!");
         }
      } else {
         sender.sendMessage(ChatColor.GOLD + sender.getName() + " can not change follow distance - must be in-game to follow.");
      }

   }

   private void handleMoveFarther(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker stalker = FollowRoster.getInstance().getStalker(sender.getName());
         if(stalker != null) {
            stalker.setDistance(stalker.getDistance() * 2);
            sender.sendMessage(ChatColor.RED + "You" + ChatColor.GOLD + " are now following at distance " + ChatColor.AQUA + stalker.getDistance());
         } else {
            this.logger.severe("Sender " + sender.getName() + " was not in the stalker list.");
            sender.sendMessage(ChatColor.RED + "Couldn\'t find you in the stalker list!");
         }
      } else {
         sender.sendMessage(ChatColor.GOLD + sender.getName() + " can not change follow distance - must be in-game to follow.");
      }

   }

   private void handleReload(CommandSender sender) {
      FollowConfig.getInstance().reloadConfig();
      sender.sendMessage(ChatColor.GOLD + "Config reloaded.");
      if(sender instanceof Player) {
         this.logger.info(sender.getName() + " reloaded the config.");
      }

   }

   @SuppressWarnings("deprecation")
private void handleFollowPlayer(CommandSender sender, Command cmd, String[] args) {
      String suspectName = null;
      Player stalker = null;
      Player suspect = null;
      int distance = FollowConfig.getInstance().getFollowDistance();
      if(sender instanceof Player) {
         stalker = (Player)sender;
         if(args.length == 0) {
            suspectName = this.getNearestPlayer(stalker);
         } else {
            suspectName = args[0];
            sender.sendMessage(ChatColor.GOLD + "Targeting nearest player: " + ChatColor.WHITE + suspectName);
         }

         if(suspectName != null) {
            suspect = Bukkit.getServer().getPlayer(suspectName);
            if(suspect == null) {
               sender.sendMessage(ChatColor.WHITE + suspectName + ChatColor.GOLD + " is not an online player.");
               sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
            } else if(suspect.getName().equalsIgnoreCase(sender.getName())) {
               sender.sendMessage(ChatColor.GOLD + "You can\'t follow yourself.");
            } else if(FollowRoster.getInstance().isSuspect(sender.getName())) {
               sender.sendMessage(ChatColor.GOLD + "You can\'t follow someone while you\'re being followed.");
            } else {
               if(args.length > 1) {
                  try {
                     distance = Integer.parseInt(args[1]);
                  } catch (NumberFormatException var9) {
                     sender.sendMessage(ChatColor.GOLD + args[1] + " was not a parseable integer - using a default value.");
                  }
               }

               FollowRoster.getInstance().follow(stalker, suspect, distance);
               sender.sendMessage(ChatColor.RED + "You" + ChatColor.GOLD + " are now following " + ChatColor.WHITE + suspect.getName() + ChatColor.GOLD + " at distance " + ChatColor.AQUA + distance);
               this.logger.info(stalker.getName() + " is now following " + suspect.getName() + " at distance " + distance);
            }
         } else {
            sender.sendMessage(ChatColor.GOLD + "There is no one to follow.");
         }
      } else if(args.length == 0) {
         this.handleFollowHelp(sender, cmd, args);
      } else {
         sender.sendMessage(ChatColor.GOLD + sender.getName() + " can not follow another player - must be in-game to follow.");
      }
   }

   private String getNearestPlayer(Player fromPlayer) {
      String nearestPlayerName = null;
      double distance = Double.MAX_VALUE;
      ArrayList<Player> players = (ArrayList<Player>)fromPlayer.getWorld().getPlayers();

      for(int i = 0; i < players.size(); ++i) {
         Player p = (Player)players.get(i);
         if(!p.getName().equalsIgnoreCase(fromPlayer.getName())) {
            Double testDistance = Double.valueOf(fromPlayer.getLocation().distance(p.getLocation()));
            if(testDistance.doubleValue() < distance) {
               nearestPlayerName = p.getName();
               distance = testDistance.doubleValue();
            }
         }
      }

      return nearestPlayerName;
   }

   private void handleFollowHelp(CommandSender sender, Command cmd, String[] args) {
      sender.sendMessage(ChatColor.GOLD + cmd.getUsage());
   }

   private void handleFollowStop(CommandSender sender) {
      if(sender instanceof Player) {
         Stalker s = FollowRoster.getInstance().unfollow((Player)sender);
         if(s != null) {
            sender.sendMessage(ChatColor.GOLD + "You are no longer following " + s.getSuspectName());
            this.logger.info(sender.getName() + " is no longer following " + s.getSuspectName());
         }
      } else {
         sender.sendMessage(sender.getName() + " is not following anyone.");
      }

   }

   private void handleFollowList(CommandSender sender, String[] args) {
      String[] listStringArray = FollowRoster.getInstance().toStringArray();
      sender.sendMessage(listStringArray);
      this.logger.info("Sent " + sender.getName() + " the list of followers.");
   }
}
