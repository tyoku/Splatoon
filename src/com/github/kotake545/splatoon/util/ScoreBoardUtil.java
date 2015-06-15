package com.github.kotake545.splatoon.util;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.github.kotake545.splatoon.Splatoon;

public class ScoreBoardUtil {
	public static String[] colors = new String[]{"aqua","black","blue","dark_aqua","dark_blue","dark_gray","dark_green","dark_purple"
			,"dark_red","gold","gray","green","light_purple","red","white","yellow"};

	public static Colors getColor(String name){
		String snd = name.toUpperCase().replace(" ", "_");
		Colors color = Colors.valueOf(snd);
		return color;
	}

	public enum Colors {
		AQUA("aqua",3,0),
		BLACK("black",15,0),
		BLUE("blue",11,0),
		DARK_AQUA("dark_aqua",9,0),
		DARK_BLUE("dark_blue",22,0),
		DARK_GRAY("dark_gray",7,0),
		DARK_GREEN("dark_green",13,0),
		DARK_PURPLE("dark_purple",10,0),
		DARK_RED("dark_red",152,0),
		GOLD("gold",1,0),
		GRAY("gray",8,0),
		GREEN("green",5,0),
		LIGHT_PURPLE("light_purple",2,0),
		RED("red",14,6),
		WHITE("white",0,0),
	    YELLOW("yellow",4,0);

	    private String Name;
	    private int wool;
	    private int clay;

	    Colors(String Name,int wool,int clay) {
	        this.Name = Name;
	        this.wool = wool;
	        this.clay = clay;
	    }

	    public Integer[] getBlock(){
	    	Integer[] block = new Integer[]{0,0};
	    	if(Splatoon.ikaConfig.inkWool){
	    		block[0] = 35;
	    		block[1] = wool;
	    		if(wool>=22){
		    		block[0] = wool;
		    		block[1] = 0;
	    		}
	    	}else{
	    		block[0] = 159;
	    		block[1] = clay;
	    	}
			return block;
	    }

	}
	public static Team addPlayerTeam(Player player, String color) {
		Scoreboard scoreboard = getMainScoreboard();
		Team team = scoreboard.getTeam(color);
		if(team == null){
			team = scoreboard.registerNewTeam(color);
			team.setDisplayName(ColorReplace(color) + color + ChatColor.RESET);
			team.setPrefix(ColorReplace(color).toString());
			team.setSuffix(ChatColor.RESET.toString());
			team.setAllowFriendlyFire(false);
		}
		team.addPlayer(player);
		player.setDisplayName(ColorReplace(color) + player.getName() + ChatColor.RESET);
//		RefreshTeamAllPlayer();
		return team;
	}

	public static void leavePlayerTeam(Player player) {
		Team team = getPlayerTeam(player);
		if (team != null){
			team.removePlayer(player);
		}
		player.setDisplayName(player.getName());
//		RefreshTeamAllPlayer();
	}

    public static ChatColor ColorReplace(String color) {
    	boolean check = false;
    	for(String name:colors){
    		if(name.equals(color)){
    			check = true;
    			break;
    		}
    	}
    	if(check){
    		return ChatColor.valueOf(color.toUpperCase());
    	}else{
    		return ChatColor.WHITE;
    	}
    }

	public static Team getPlayerTeam(Player player) {
		Scoreboard scoreboard = getMainScoreboard();
		Set<Team> teams = scoreboard.getTeams();
		for (Team team : teams) {
			if(team!=null){
				for (OfflinePlayer p : team.getPlayers()) {
					if (p.getName().equalsIgnoreCase(player.getName())) {
						return team;
					}
				}
			}
		}
		return null;
	}
    public static Scoreboard getMainScoreboard() {
        return Splatoon.instance.getServer().getScoreboardManager().getMainScoreboard();
    }
}
