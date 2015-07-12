package com.github.kotake545.splatoon.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
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
//	List<String> a = new ArrayList<String>();
//	a.add(player.getScoreboard().getTeam(paramString)+""+ChatColor.BOLD+" Splatoon ");
//	a.add("-------------"+ChatColor.DARK_GREEN);
//	a.add(ChatColor.DARK_GREEN+"Time: "+ChatColor.WHITE+"00:00");
//	a.add(" ");
//	a.add(ChatColor.GOLD+"Points: "+ChatColor.WHITE+"0000");
//	a.add(ChatColor.GREEN+"Special:"+ChatColor.WHITE+"000%");
//	a.add(ChatColor.GREEN+"▮▮▮▮"+ChatColor.GRAY+"▮▮▮▮▮▮▮▮");
//	a.add("-------------"+ChatColor.DARK_AQUA);
//	ScoreBoardUtil.setSidebar(player,a, true);

	public static void setSidebar(Player player,List<String> side,boolean display){
		Scoreboard sb = player.getScoreboard();
		Objective obj = sb.getObjective("ika");
		if ( obj == null ) {// 取得できなかったら新規作成する
			obj = sb.registerNewObjective("ika", "dummy");
		}
		if(display){
//			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		    //含まれないものは削除
		    for(String string:sb.getEntries()){
		    	boolean flag = false;
		    	for(String a:side){
		    		if(a.equals(string)){
		    			flag=true;
				        break;
		    		}
		    	}
		    	if(flag){
		    		continue;
		    	}
		    	sb.resetScores(string);
		    }
		    if(side.size()<=0){
		    	return;
		    }
		    if(!obj.getDisplayName().equals(side.get(0))){
			    obj.setDisplayName(side.get(0));
		    }
		    int score = side.size()-1;
		    int max = 15;
		    for(int point=0;1<=score;){
				if(max<=point){
					break;
				}
				String word = side.get(score);
				if(point==0){
					obj.getScore(word).setScore(1);
				}
				if(obj.getScore(word).getScore()!=point){
					obj.getScore(word).setScore(point);
				}
				point++;
				score--;
			}
		}
	}


	public static Team addPlayerTeam(Player player, String color) {
		Team team =  addPlayerTeam(getMainScoreboard(), player, color);
		for(Player send:Splatoon.getOnlinePlayers()){
			addPlayerTeam(send.getScoreboard(), player, color);
		}
		return team;
	}

	public static Team addPlayerTeam(Scoreboard scoreboard,Player player, String color) {
		Team team = scoreboard.getTeam(color);
		if(team == null){
			team = scoreboard.registerNewTeam(color);
			team.setDisplayName(ColorReplace(color) + color + ChatColor.RESET);
			team.setPrefix(ColorReplace(color).toString());
			team.setSuffix(ChatColor.RESET.toString());
		}
		team.addPlayer(player);
		team.setAllowFriendlyFire(false);
		team.setCanSeeFriendlyInvisibles(true);
		player.setDisplayName(ColorReplace(color) + player.getName() + ChatColor.RESET);
		return team;
	}

	public static void leavePlayerTeam(Player player) {
		leavePlayerTeam(getMainScoreboard(), player);
		for(Player send:Splatoon.getOnlinePlayers()){
			leavePlayerTeam(send.getScoreboard(), player);
		}
	}

	public static void leavePlayerTeam(Scoreboard scoreboard,Player player) {
		Team team = getPlayerTeam(scoreboard,player);
		if (team != null){
			team.removePlayer(player);
		}
		player.setDisplayName(player.getName());
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

	public static void setRandomTeam(List<String> list){
		ArrayList<Player> players = new ArrayList<Player>();
		for(Player player:Splatoon.getOnlinePlayers()){
			if (player.getGameMode()==GameMode.CREATIVE){
				continue;
			}
		}
		for ( Player p : Bukkit.getOnlinePlayers()) {
			if ( p.getGameMode() == GameMode.CREATIVE){
				continue;
			}
			players.add(p);
		}
		Random rand = new Random();
		for ( int i=0; i<players.size(); i++ ) {
			int a = rand.nextInt(players.size());
			Player u = players.get(i);
			players.set(i, players.get(a));
			players.set(a, u);
		}
		for ( int i=0; i<players.size(); i++ ) {
			int r = i % list.size();
			addPlayerTeam(players.get(i),list.get(r));
			players.get(i).sendMessage(Splatoon.format+"あなたは["+ColorReplace(list.get(r))+list.get(r)+ChatColor.RESET+"]チームに決定しました。");
		}
	}

    public static void SpawnTeleport(Player player){
    	Location location = getSpawnLocation(player);
    	player.teleport(location);
    }
    public static Location getSpawnLocation(Player player){
    	Location location = player.getWorld().getSpawnLocation();
    	if(Splatoon.ikaConfig.spawnLocation!=null){
    		location = Splatoon.ikaConfig.spawnLocation;
    	}
//    	player.getBedSpawnLocation()
    	Team team = getPlayerTeam(getMainScoreboard(),player);
    	if(team!=null){
    		if(Splatoon.MainTask.getGameStatus().equals("countdown")||Splatoon.MainTask.getGameStatus().equals("gametime")){
    			Location spawn = Splatoon.MainTask.gameStage.getSpawn(team.getName());
    			if(spawn!=null){
    				location = spawn;
    			}
    		}
    	}
//    	if(location==null){
//    		for(World world:Bukkit.getServer().getWorlds()){
//    			if(world.getSpawnLocation()!=null){
//    				location = world.getSpawnLocation();
//    			}
//    		}
//    	}
    	return location;
    }

	public static Team getPlayerTeam(Scoreboard scoreboard,Player player) {
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
