package com.github.kotake545.splatoon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.github.kotake545.splatoon.Manager.IkaStageManager;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class IkaStage {
	public String stageName;
	public Hashtable<String,Location> teams;
	public int gameStart = 30;
	public int countDown = 10;
	public int gameTime = 180;


	private int timer = 0;
	public boolean gameStartActive = false;
	public boolean countDownActive = false;
	public boolean gameTimeActive = false;

	public void timer() {
		timer --;
		int count = timer;

		if(count <= 5 || count == 10|| count == 30 || count%60==0){
			for(Player player:Splatoon.getOnlinePlayers()){
				player.sendMessage(Splatoon.format+""+count+"秒");
			}
		}
		//カウントダウン
		if(timer<=0){
			if(gameStartActive){
				this.gameStartActive = false;
				onCountDown();
				return;
			}
			if(countDownActive){
				this.countDownActive = false;
				onGameTime();
				return;
			}
			if(gameTimeActive){
				this.gameTimeActive = false;
				onFinish();
				return;
			}
		}
	}

	public int getTimer(){
		return timer;
	}


	public IkaStage clone(){
		IkaStage clone = new IkaStage();
		clone.stageName = this.stageName;
		clone.teams = this.teams;
		clone.gameStart = this.gameStart;
		clone.countDown = this.countDown;
		clone.gameTime = this.gameTime;
		return clone;
	}

	public IkaStage(){
		this.teams = new Hashtable<String, Location>();
		this.gameStart = 30;
		this.countDown = 10;
		this.gameTime = 180;
	}

	public void setTeamAndSpawn(String team, String spawn) {
		String teamName = team.substring(team.indexOf("[")+1);
		teamName = teamName.substring(0,teamName.lastIndexOf("]"));
		teams.put(teamName,getLocation(spawn));
	}

	public Location getLocation(String deta){
		String[] s = deta.split("/");
		World world = Bukkit.getWorld(s[0]);
		double x = Double.parseDouble(s[1]);
		double y = Double.parseDouble(s[2]);
		double z = Double.parseDouble(s[3]);
		float yaw = Float.parseFloat(s[4]);
		float pitch = Float.parseFloat(s[5]);
		Location location = new Location(world, x, y, z, yaw, pitch);
		return location;
	}
	public void onFinish(){
		//まだ


		//この前に予め結果を用意しておく。
		Splatoon.MainTask.gameStage = null;
		Splatoon.blockUtil.returnBeforeBlocks();
		for(Player player:Splatoon.getOnlinePlayers()){
			ScoreBoardUtil.leavePlayerTeam(player);
			Utils.heal(player);
			Splatoon.ikaManager.getIka(player).Reset();
			if(player.getGameMode()!=GameMode.CREATIVE){
				ScoreBoardUtil.SpawnTeleport(player);
			}
			player.sendMessage(Splatoon.format+"ゲームが終了しました。");
		}
	}

	public String getName() {
		return this.stageName;
	}
	public void onGameTime(){
		for(Player player:Splatoon.getOnlinePlayers()){
			player.sendMessage(Splatoon.format+"ゲームが開始しました。");
		}
		this.gameTimeActive = true;
		this.timer = gameTime;
	}

	public void onCountDown(){
		this.countDownActive = true;
		this.timer = countDown;
		List<String> list = new ArrayList<String>();
		for(String team:teams.keySet()){
			list.add(team);
		}
		ScoreBoardUtil.setRandomTeam(list);
		for(Player player:Splatoon.getOnlinePlayers()){
			if (player.getGameMode()==GameMode.CREATIVE){
				continue;
			}
			ScoreBoardUtil.SpawnTeleport(player);
		}
		for(Player player:Splatoon.getOnlinePlayers()){
			Utils.heal(player);
			Splatoon.ikaManager.getIka(player).Reset();
			Splatoon.ikaManager.getIka(player).onClass();
			player.sendMessage(Splatoon.format+"ゲーム開始のカウントダウンが始まりました。");
		}
	}

	public void onGameStart(){
		this.gameStartActive = true;
		this.timer = gameStart;
		for(Player player:Splatoon.getOnlinePlayers()){
			player.sendMessage(Splatoon.format+"ゲーム開始までの待機時間が始まりました。");
		}
	}

	public Location getSpawn(String team) {
		if(teams.containsKey(team)){
			return teams.get(team);
		}
		return null;
	}
	public void saveGameTime(int secconds) {
		this.gameTime=secconds;
		try {
			onEditYml("gametime",String.valueOf(secconds));
		} catch (IOException e) {
		}
	}
	public void saveCountDown(int secconds) {
		this.countDown=secconds;
		try {
			onEditYml("countdown",String.valueOf(secconds));
		} catch (IOException e) {
		}
	}
	public void saveGameStart(int secconds) {
		this.gameStart=secconds;
		try {
			onEditYml("gamestart",String.valueOf(secconds));
		} catch (IOException e) {
		}
	}
	public void saveSpawn(String teamName, Location location) {
		teams.put(teamName, location);
		String world=location.getWorld().getName();
		String x = String.valueOf(location.getX());
		String y = String.valueOf(location.getY());
		String z = String.valueOf(location.getZ());
		String yaw = String.valueOf(location.getYaw());
		String pitch = String.valueOf(location.getPitch());
		try {
			onEditYml("spawnpoint["+teamName+"]",world+"/"+x+"/"+y+"/"+z+"/"+yaw+"/"+pitch);
		} catch (IOException e) {
		}
	}
	public void onEditYml(String load,String set) throws IOException{
		File file = getFile();
		ArrayList<String> line = new ArrayList<String>();
		FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		while((strLine = br.readLine()) != null){
			line.add(strLine);
		}
		br.close();
		in.close();
		fstream.close();

		boolean check = false;
		int count = 0;
		for(String string:line){
			if(string.indexOf("=") <= 0){
				count++;
				continue;
			}
			String l = string.substring(0,string.indexOf("=")).toLowerCase();
			if(l.equals(load)){
				line.set(count,load+"="+set);
				check = true;
				break;
			}
			count++;
		}
		if(!check){//なかった場合追加
			line.add(load+"="+set);
		}
		file.delete();//削除
		//新規作成
		String path = Splatoon.getPluginFolder() + "/stages";
		File newyml = new File(path + "/" + stageName+".yml");
		newyml.createNewFile();
		FileWriter edit = new FileWriter(newyml, true);
		String n = System.getProperty("line.separator");
		for(String write:line){
			edit.write(write+n);
		}
		edit.close();
	}
	public File getFile(){
		return IkaStageManager.getStageFile(stageName);
	}
}
