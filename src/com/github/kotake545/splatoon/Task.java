package com.github.kotake545.splatoon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.util.ScoreBoardUtil;


public class Task {
	private int ticks;
	public IkaStage gameStage = null;

	public Task() {
		this.ticks = 0;
		this.gameStage = null;
	}

	public void timer(){
		ticks++;
		if(gameStage!=null){
			if(ticks%20==0){
				gameStage.timer();

//				for(Player player:Splatoon.getOnlinePlayers()){
//					setScoreBoard(player);
//				}
			}
		}
		for(Player player:Splatoon.getOnlinePlayers()){
			setScoreBoard(player);
		}
	}

	public void setScoreBoard(Player player){
		List<String> a = new ArrayList<String>();
		if(gameStage!=null){
			if(gameStage.gameStartActive){
				a.add("ゲーム開始まで:"+gameStage.getTimer()+"秒");
				a.add(" ");
			}else if(gameStage.countDownActive){
				a.add("カウントダウン:"+ChatColor.RED+""+ChatColor.BOLD+gameStage.getTimer());
				a.add(" ");
			}else if(gameStage.gameTimeActive){
				a.add(ChatColor.AQUA+""+ChatColor.BOLD+" Splatoon ");
				a.add("-------------"+ChatColor.DARK_GREEN);
				a.add(ChatColor.DARK_GREEN+"Time: "+ChatColor.WHITE+replaceMinutes(gameStage.getTimer()));
				a.add(" ");
				a.add(ChatColor.GOLD+"Points:"+ChatColor.WHITE+getPoints(player));
				a.add(ChatColor.GREEN+"Special:"+ChatColor.WHITE+"000%");
				a.add(ChatColor.GREEN+""+ChatColor.GRAY+"▮▮▮▮▮▮▮▮▮▮▮▮");
				a.add("-------------"+ChatColor.DARK_AQUA);
			}
		}
		ScoreBoardUtil.setSidebar(player,a, true);
	}
	//ポイント表記を返す。
	public String getPoints(Player player){
		int point = Splatoon.ikaManager.getIka(player).point;
		if(point>=99999){
			return "99999";
		}
		if(point>=10000){
			return String.valueOf(point);
		}
		if(point>=1000){
			return " "+String.valueOf(point);
		}
		if(point>=100){
			return String.valueOf(" 0"+point);
		}
		if(point>=10){
			return String.valueOf(" 00"+point);
		}
		if(point>=0){
			return String.valueOf(" 000"+point);
		}
		return "";
	}

	public static String replaceMinutes(int secconds){
		String ti = "";
		String mi = "";
    	int min = 0;
    	int time = secconds;
    	if(time >= 60){
    		min = time/60;
    		time = time -min*60;
    	}
    	ti = ""+time;
    	mi = ""+min;
    	if(time<10){
    		ti = "0"+ti;
    	}
    	if(min<10){
    		mi = "0"+mi;
    	}
    	return mi+":"+ti;
	}

	public String getGameStatus(){
		if(gameStage!=null){
			if(gameStage.gameStartActive)return "gamestart";
			if(gameStage.countDownActive)return "countdown";
			if(gameStage.gameTimeActive)return "gametime";
		}
		return "false";
	}
	public boolean gameStart(IkaStage stage){
		if(gameStage!=null){
			return false;
		}
		stage.onGameStart();
		this.gameStage=stage;
		return true;
	}

	public boolean gameEnd() {
		if(gameStage!=null){
			gameStage.onFinish();
			return true;
		}
		return false;
	}
}
