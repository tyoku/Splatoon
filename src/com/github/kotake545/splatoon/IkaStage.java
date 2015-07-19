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
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.kotake545.splatoon.Manager.IkaStageManager;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class IkaStage {

	public String stageName;
	public Hashtable<String,Location> teams;
	public int gameStart = 30;
	public int countDown = 10;
	public int gameTime = 180;

	private int ticks =0;
	private int timer = 0;
	public boolean gameStartActive = false;
	public boolean countDownActive = false;
	public boolean gameTimeActive = false;
	public boolean result = false;

	public List<String> teamNames = new ArrayList<String>();
	//全部の合計
	public int allPoint;
	//発表されているポイント
	public double resultpoint;
	//終わりフラグ
	public boolean finish = false;

	Hashtable<String,Integer> teamPoints = new Hashtable<String,Integer>();

	//Delay
	public int resultDelay=30;
	//Delay
	public int finishDelay=60;

	public void tick(){
		ticks++;
		if(ticks%20==0){
			timer();
		}
		//結果発表を回す
		if(result){
			setResult();
		}
	}
	public void setResult(){
		timer=99;
		//2チームの時
		if(teamNames.size()==2){
			Result2Team();
		}else{
			finish=true;
		}
//		teamPoints;
		//渡すのは a/100.0  b/100.0


		/**
		 * 集計が終わったらfinish
		 */
		if(finish){
			if(finishDelay>0){
				finishDelay--;
			}else{
				result=false;
				onFinish();
			}
		}
	}
	/**
	 * 1tick毎に実行されてる
	 */
	public void Result2Team(){
		if(finish){
			return;
		}
		String teamA = teamNames.get(0);
		String teamB = teamNames.get(1);
		int a = teamPoints.get(teamA);
		int b = teamPoints.get(teamB);
		int min = Math.min(a, b);
		int max = Math.max(a, b);
		double add = min/100;
		if(resultpoint<=min&&0<add){
			//両方共resultpointを使う。

			setResultInventryAllPlayer(getResult(teamA,resultpoint,this.allPoint,resultpoint,teamB));
			if(ticks%10==0){
				Utils.playSoundAllPlayer(Sound.LAVA_POP,1,2);
			}
			resultpoint+=add;
		}else{
			resultDelay-=1;
			if(resultDelay<=0){
				//最終結果発表
				setResultInventryAllPlayer(getResult(teamA,a,this.allPoint,b,teamB));
				Utils.playSoundAllPlayer(Sound.EXPLODE,1,2);

				String win = "";
				for(String teamName:teamNames){
					if(teamPoints.get(teamName)==max){
						win=teamName;
						break;
					}
				}
				for(Player player:Splatoon.getOnlinePlayers()){
					player.sendMessage(Splatoon.format+"勝利チーム:"+win);
				}

				this.finish=true;
			}
		}
	}
	public void setResultInventryAllPlayer(String name){
		for(Player player:Splatoon.getOnlinePlayers()){
			if(player.getGameMode()==GameMode.CREATIVE){
				continue;
			}
			setResultInventry(player, name);
		}
	}

	/**
	 * リザルト用のアイテムを渡す
	 * @param player
	 */
	public void setResultInventry(Player player,String name){
		ItemStack item = getResultItem(name);
		player.getInventory().setItem(0,item);
		player.getInventory().setItem(1,item);
		player.getInventory().setItem(2,item);
		player.getInventory().setItem(3,item);
		player.getInventory().setItem(4,item);
		player.getInventory().setItem(5,item);
		player.getInventory().setItem(6,item);
		player.getInventory().setItem(7,item);
		player.getInventory().setItem(8,item);
		player.updateInventory();
	}

	private ItemStack getResultItem(String name) {
		ItemStack itemStack = new ItemStack(102);
//		itemStack.setDurability((byte)(int)weaponID[1]);
		itemStack = IkaWeapon.setName(itemStack,name);
		return itemStack;
	}
	private String getResult(String Acolor,double resultpoint2,double All,double resultpoint3,String Bcolor){
		ChatColor a = ScoreBoardUtil.ColorReplace(Acolor);
		ChatColor b = ScoreBoardUtil.ColorReplace(Bcolor);
		return getResult(a, resultpoint2, All,resultpoint3,b);
	}

	/**
	 * 左のポイント 全体のポイント 右のポイント
	 * @param timer
	 * @param time
	 * @param chatColor
	 * @return
	 */
	private String getResult(ChatColor Acolor,double A,double All,double B,ChatColor Bcolor){
		double maxlength = 100;
		double x = maxlength/All;
		int a = (int) (A*x); //Aチーム point%
		int c = (int) (B*x); //Bチーム point%
		int b = (int) (maxlength-a-c);
		String a1 = Acolor+"";
		for(int i=0;i<a;i++){
			a1+="│";
		}
		String b1 = ChatColor.DARK_GRAY+"";
		for(int i=0;i<b;i++){
			b1+="│";
		}
		String c1 = Bcolor+"";
		for(int i=0;i<c;i++){
			c1+="│";
		}
		return  a1+b1+c1;
	}

	/**
	 * 一秒に一回呼び出されるイベント
	 */
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
				onResult();
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

	public static Location getLocation(String deta){
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
			IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
			ika.setSpectator(false);
			int point = Splatoon.ikaManager.getIka(player).point;
			ScoreBoardUtil.leavePlayerTeam(player);
			Utils.heal(player);
			Splatoon.ikaManager.getIka(player).Reset();
			if(player.getGameMode()!=GameMode.CREATIVE){
				ika.onClass();
				player.setExp(0);
				player.setLevel(0);
				ScoreBoardUtil.SpawnTeleport(player);
			}
			player.sendMessage(Splatoon.format+"ゲームが終了しました。");
			Splatoon.ikaManager.getIka(player).point = 0;
//			Bukkit.broadcastMessage(Splatoon.format+"");
			player.sendMessage(Splatoon.format+"貴方のポイント数は"+point+"でした。(テスト)");
		}
	}

	public String getName() {
		return this.stageName;
	}

	/**
	 * ゲーム終了後。結果発表に使う。
	 */
	public void onResult (){
		this.teamPoints = getTeamPoints();
		//パーセンテージも求めておく
		int maxPoint = 0;
		for(String a:teamPoints.keySet()){
			maxPoint+=teamPoints.get(a);
		}
		this.allPoint=maxPoint;

		for(Player player:Splatoon.getOnlinePlayers()){
			if(player.getGameMode()==GameMode.CREATIVE){
				continue;
			}
			//全員観戦者にする
			player.playSound(player.getLocation(),Sound.LEVEL_UP,1,-1);
			player.sendMessage(ChatColor.GOLD+""+ChatColor.BOLD+"|||=Finish!=|||=Finish!=|||=Finish!=|||=Finish!=|||=Finish!=|||=Finish!=|||");
			IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
			ika.setSpectator(true);
			player.getInventory().clear();
			player.updateInventory();
		}
		this.result = true;
		timer=99;
	}

	public void onGameTime(){
		for(Player player:Splatoon.getOnlinePlayers()){
			player.sendMessage(Splatoon.format+"ゲームが開始しました。");
			player.playSound(player.getLocation(),Sound.ZOMBIE_INFECT, 1,2);
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
		this.teamNames=list;
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
		onEditYml("gametime",String.valueOf(secconds));
	}
	public void saveCountDown(int secconds) {
		this.countDown=secconds;
		onEditYml("countdown",String.valueOf(secconds));
	}
	public void saveGameStart(int secconds) {
		this.gameStart=secconds;
		onEditYml("gamestart",String.valueOf(secconds));
	}
	public void saveSpawn(String teamName, Location location) {
		teams.put(teamName, location);
		String world=location.getWorld().getName();
		String x = String.valueOf(location.getX());
		String y = String.valueOf(location.getY());
		String z = String.valueOf(location.getZ());
		String yaw = String.valueOf(location.getYaw());
		String pitch = String.valueOf(location.getPitch());
		onEditYml("spawnpoint["+teamName+"]",world+"/"+x+"/"+y+"/"+z+"/"+yaw+"/"+pitch);
	}
	public void onEditYml(String load,String set){
		try {
			onEditYml(getFile(),stageName,Splatoon.getPluginFolder() + "/stages",load,set);
		}catch (IOException e) {
		}
	}

	public Hashtable<String,Integer> getTeamPoints(){
		Hashtable<String,Integer> teamPoint = new Hashtable<String, Integer>();
		for(String name:this.teams.keySet()){
			teamPoint.put(name,getTeamPoint(name));
		}
		return teamPoint;
	}
	/**
	 * チームが塗ったブロック数を返す。
	 * @param teamName
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int getTeamPoint(String teamName){
		int point = 0;
		Integer[] block = ScoreBoardUtil.getColor(teamName).getBlock();
		for(Block b:Splatoon.blockUtil.Blocks.keySet()){
			if(b.getTypeId()==block[0]&&b.getData()==block[1]){
				point++;
			}
		}
		return point;
	}

	/**
	 * YML編集用。
	 * @param file
	 * @param fileName
	 * @param path
	 * @param load
	 * @param set
	 * @throws IOException
	 */
	public static void onEditYml(File file,String fileName,String path,String load,String set) throws IOException{
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
		File newyml = new File(path + "/" + fileName+".yml");
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
