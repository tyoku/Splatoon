package com.github.kotake545.splatoon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.github.kotake545.splatoon.Manager.IkaWeaponManager;
import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class IkaPlayerInfo {
	public Player player;
	public String name;

	public Scoreboard scoreboard = null;

	public int point;

	public boolean isIka = false;
	public boolean canFly = false;
	public boolean swim = false;
	public int NoDamageTick;
	public int muteki;

	public int healDelay = 0;
	public int healSpeed = 0;

	public int inkGauge = 0;
	public Integer[] type = new Integer[]{0,0};

	public int tick = 0;
	public int returndelay = 0;
	public int jumping = 0;

	public IkaWeapon weapon;
	public List<IkaWeapon> weapons;
	public ItemStack lasthandItem;
	public String clickType;
	private boolean lastSwim;
	private float lastspeed = Splatoon.ikaConfig.moveSpeed;
	public Location lastLocation;

	private boolean isHave;
	private boolean spectator;

	public IkaClass SelectClass;

//	private Squid squid = null;
//	private boolean squidTeleport = false;

	public IkaPlayerInfo(Player player) {
		this.player = player;
		this.name = ChatColor.stripColor(player.getName());
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective("ika", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
		this.isIka = false;
		this.NoDamageTick = 0;
		this.muteki = 0;
		this.spectator = false;
		setSpectator(false);
		this.canFly = false;
		this.swim = false;
		this.jumping = 0;
		this.tick = 0;
		this.returndelay = 0;
		this.type = new Integer[]{0,0};
		this.inkGauge = Splatoon.ikaConfig.inkGauge;
		this.weapon = null;
		this.point = 0;
		this.lasthandItem = player.getItemInHand();
		this.lastLocation = player.getLocation().clone();
		player.setFoodLevel(2);
		this.clickType = "none";
		this.SelectClass = Splatoon.ikaClassManager.getClass(Splatoon.ikaConfig.defaultClass);
		this.weapons = Splatoon.ikaWeaponManager.getAllWeapons();
		for(int i = 0; i < weapons.size(); i++){
			weapons.get(i).ika = this;
		}
	}

	/**
	 * 初期化
	 */
	public void Reset(){
//		this.point = 0;
		this.spectator = false;
		this.canFly = false;
		this.swim = false;
		this.jumping = 0;
		this.returndelay = 0;
		this.type = new Integer[]{0,0};
		this.inkGauge = Splatoon.ikaConfig.inkGauge;
		this.weapon = null;
		this.lasthandItem = player.getItemInHand();
		player.setFoodLevel(2);
		this.clickType = "none";
		this.weapons = Splatoon.ikaWeaponManager.getAllWeapons();
		for(int i = 0; i < weapons.size(); i++){
			weapons.get(i).ika = this;
		}
		setSpectator(false);
	}

	@SuppressWarnings("deprecation")
	public void tick(){
		tick++;
		returndelay++;
		if(player==null||!player.isOnline()){
			return;
		}
		if(isJumping()){
			jumping--;
		}
		if(isNoDamage()){
			NoDamageTick--;
		}
		if(isMuteki()){
			muteki--;
		}
		if(player.getGameMode()==GameMode.CREATIVE){
			player.setWalkSpeed(Splatoon.ikaConfig.moveSpeed);
			return;
		}
		if(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player)==null){
			if(canFly){
				player.setAllowFlight(true);
				player.setFlying(true);
			}else{
				player.setAllowFlight(false);
				player.setFlying(false);
			}
			Collection<PotionEffect> effects = player.getActivePotionEffects();
			for ( PotionEffect e : effects ) {
				player.removePotionEffect(e.getType());
			}
			swim=false;
			setLength();
			player.setWalkSpeed(Splatoon.ikaConfig.moveSpeed);
			return;
		}
		if(Splatoon.MainTask.getGameStatus().equals("countdown")){
			lastSwim=true;
			return;
		}
		if(isSpectator()){
			player.setWalkSpeed(Splatoon.ikaConfig.moveSpeed);
			lastSwim=true;
			weapon = null;
			return;
		}
		if(player.getLocation().clone().add(0,0.1,0).getBlock().getType().getId()==9){
			setNoDamageTick(0);
			player.damage(100);
			return;
		}
		onSwim();
		if(canFly){
			player.setAllowFlight(true);
			player.setFlying(true);
		}else{
			player.setAllowFlight(false);
			player.setFlying(false);
		}
		if(Splatoon.ikaConfig.ikaChangeforSneak&&player.isOnGround()){
			if(!player.isSneaking()){
				isIka=false;
			}
		}
		boolean poisonFlag = false;
		weaponTick();
		if(isIka){
			//武器を消す。
			if(isHave&&Splatoon.ikaConfig.ikaChangeforSneak){
				player.getInventory().clear();
				isHave=false;
			}
			onINVISIBILITY(true);
			if(swim){
				if(lastSwim){
					//チャポン
					ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_CRACK.setItemIDandData(this.type[0],this.type[1]),player.getLocation().clone().add(0,0,0),0.0F,0F,0.0F,0,10);
					player.getLocation().getWorld().playSound(player.getLocation(),Sound.SWIM,(float)1,2);
					lastSwim=false;
				}
				if(tick%Splatoon.ikaConfig.inkHealSpeed==0&&inkGauge<Splatoon.ikaConfig.inkGauge){
					inkGauge+=Splatoon.ikaConfig.inkHeal;
					if(inkGauge>Splatoon.ikaConfig.inkGauge){
						inkGauge=Splatoon.ikaConfig.inkGauge;
					}
				}
				//エフェクト
				if(lastLocation.getWorld()==player.getWorld()&&0<lastLocation.distance(player.getLocation())){
//					if(Splatoon.ikaConfig.ikaSpeed<lastLocation.distance(player.getLocation())){
//						player.getLocation().getWorld().playSound(player.getLocation(),Sound.SILVERFISH_WALK,(float)0.5,1);
//					}
					ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_CRACK.setItemIDandData(this.type[0],this.type[1]),player.getLocation().clone().add(0,0,0),0.0F,0F,0.0F,0,10);
				}
				lastLocation = player.getLocation().clone();
				lastspeed =  Splatoon.ikaConfig.ikaSpeed;
			}else{
				//潜ってない時は姿が現れる。
				onINVISIBILITY(false);

				if(!lastSwim){
					player.getLocation().getWorld().playSound(player.getLocation(),Sound.SWIM,(float)1,1);
					lastSwim=true;
				}
				//潜ってから飛んでる間はspeedは変わらない。
				if(player.isOnGround()){
					lastspeed = 0.01F;
				}

				if(player.isOnGround()){
					Integer[] block = Utils.getBlock(player.getLocation().clone().add(0,-0.1,0));
					if(checkEnemyTeamBlock(block)){
						lastspeed = Splatoon.ikaConfig.downSpeed;
						player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,9999,0));
						poisonFlag=true;
					}
				}
			}
		}else{
			if(!isHave){
				onClass();
			}
			if(!lastSwim){
				player.getLocation().getWorld().playSound(player.getLocation(),Sound.SWIM,(float)1,1);
				lastSwim=true;
			}
			lastspeed = Splatoon.ikaConfig.moveSpeed;
			onINVISIBILITY(false);
			if(player.isOnGround()){
				Integer[] block = Utils.getBlock(player.getLocation().clone().add(0,-0.1,0));
				if(checkEnemyTeamBlock(block)){
					lastspeed = Splatoon.ikaConfig.downSpeed;
					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,9999,0));
					poisonFlag=true;
				}
			}
		}
		//当たり判定の変更
		setLength();
//		if(squidTeleport){
//			if(squid!=null){
//				squid.teleport(player.getLocation());
//				squid.setVelocity(player.getVelocity());
//			}else{
//				squid = (Squid) player.getWorld().spawnEntity(player.getLocation(), EntityType.SQUID);
//				squid.teleport(player.getLocation());
//				squid.setVelocity(player.getVelocity());
//			}
//		}else if(squid!=null){
//			squid.remove();
//			squid=null;
//		}
		if(!poisonFlag){
			player.removePotionEffect(PotionEffectType.POISON);
		}
//		if(player.isSneaking()&&(swim||!player.isOnGround())){
//			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,0,10));
//		}else{
//			player.removePotionEffect(PotionEffectType.SPEED);
//		}
		if(isIka&&player.isSneaking()){
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,0,10));
		}else{
			player.removePotionEffect(PotionEffectType.SPEED);
		}

		if(weapon!=null&&weapon.addMoveSpeed!=0.00F){
			lastspeed+=weapon.addMoveSpeed;
		}
		if(player.getWalkSpeed()!=lastspeed){
			player.setWalkSpeed(lastspeed);
		}

		healTick();
		ReflashBlock();
		ReflashInk();
		downstep();
	}


	public void healTick(){
		healDelay--;
		healSpeed--;
		if(player.getHealth()>=player.getMaxHealth()){
			return;
		}
		if(healDelay<=0&&healSpeed<=0){
			Utils.addHealth(player,Splatoon.ikaConfig.heal);
			healSpeed=Splatoon.ikaConfig.healSpeed;
		}
	}

	public void weaponTick(){
		ItemStack hand = player.getItemInHand();
		if(hand != null){
			if(!Splatoon.ikaConfig.ikaChangeforSneak){
				if(hand.getTypeId()==0){
					isIka=true;
				}else{
					isIka=false;
				}
			}
			IkaWeapon iw = getWeapon(IkaWeaponManager.getItemDisplayName(hand));
			weapon = iw;
			if(iw == null||lasthandItem!=hand){
				player.removePotionEffect(PotionEffectType.SLOW);
				player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
			}
			if(iw!=null){
				iw.setItemStack(hand);
				if(iw.miningFatigue>0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,100,iw.miningFatigue-1));
				}
			}
			lasthandItem=hand;
		}else{
			isIka=false;
			weapon = null;
			lasthandItem = null;
			player.removePotionEffect(PotionEffectType.SLOW);
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}
		for(int i = weapons.size() - 1; i >= 0; i--){
			IkaWeapon iw = weapons.get(i);
			if(iw != null){
				iw.tick();
			}
		}
	}

	public IkaWeapon getWeapon(String itemDisplayName) {
		for(int i = weapons.size() - 1; i >= 0; i--){
			if(weapons.get(i).getName().toLowerCase().equals(itemDisplayName.toLowerCase())){
				return weapons.get(i);
			}
		}
		return null;
	}
	public boolean onClick(String type){
		if(isIka){
			return false;
		}
		if(weapon!=null){
			weapon.onClick(type);
			return true;
		}else{
		}
		return false;
	}

	private void ReflashBlock() {
		if(!Splatoon.ikaConfig.blockset){
			for(Block block:Splatoon.blockUtil.Blocks.keySet()){
				Integer[] b = Splatoon.blockUtil.getBlock(block);
				player.sendBlockChange(block.getLocation(),b[0],(byte)(int)b[1]);
			}
		}
	}

	public void onIka(){
		if(Splatoon.ikaConfig.ikaChangeforSneak){
			if(!player.isSneaking()){
				isIka=true;
			}else{
				if(!player.isOnGround()&&isIka){
					if(checkWall()){
						isIka=true;
						return;
					}
				}
				isIka=false;
			}
			return;
		}
	}

	@SuppressWarnings("deprecation")
	public void onSwim(){
		canFly = false;
		swim = false;
		/**
		 * イカ壁同化中...
		 */
		if(isIka){
			Location loc = player.getLocation();
			if(player.isOnGround()){
				Integer[] block = Utils.getBlock(loc.clone().add(0,-0.1,0));
				if(checkTeamBlock(block)){
					swim = true;
					returndelay = 0;
				}else{
					if(returndelay<10&&isJumping()){
						swim = true;
					}else
					if(returndelay<15&&Utils.getBlock(loc.clone().add(0,-0.1,0))[0]==0){
						swim = true;
					}else
					//ここの数はガクガクなるの防止なのでイカ状態の移動速度によって変更していいかも 速いほど少なく
					if(returndelay<3&&player.isSneaking()){
						swim = true;
					}
//					if(swim&&block[0]!=0&&Utils.getBlock(loc.clone())[0]==0){
//						swim = false;
//					}
				}
			}else{
				if(checkWall()){
					canFly = true;
					swim = true;
				}else{
					canFly = false;
				}
			}
		}
	}
	public void downstep(){
		if(player.isSneaking()&&Splatoon.ikaConfig.downSteps){
			if(isIka&&player.isOnGround()&&!isJumping()&&player.getLocation().clone().add(0,-0.1,0).getBlock().getTypeId()==0&&player.getLocation().clone().add(0,-1.1,0).getBlock().getTypeId()!=0){
				player.teleport(player.getLocation().clone().add(0,-1,0));
			}
		}
	}

	public void ReflashInk(){
		if(true){//ゲージタイプのチェック
			Utils.setEXPBar(player,Splatoon.ikaConfig.inkGauge,inkGauge);
		}
	}

	public boolean checkWall(){
		if(checkWall(0.5, 0)||checkWall(-0.5, 0)||checkWall(0,0.5)||checkWall(0,-0.5)){
			return true;
		}else{
			return false;
		}
	}
	public boolean checkWall(double x,double z){
		if(checkTeamBlock(Utils.getBlock(player.getLocation().clone().add(x,0.1,z)))){
			Location eye = player.getEyeLocation().clone().add(x,0,z);
			if(checkTeamBlock(Utils.getBlock(eye))||eye.getBlock()!=null&&eye.getBlock().getTypeId()==0){
				return true;
			}
		}
		return false;
	}

	public Integer[] getTeamBlock(){
		if(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player) != null){
			String teamname = ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player).getName();
			return ScoreBoardUtil.getColor(teamname).getBlock();
		}
		return null;
	}

	public boolean checkTeamBlock(Integer[] integers){
		if(integers!=null){
			if(ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player) != null){
				String teamname = ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player).getName();
				if(ScoreBoardUtil.getColor(teamname).getBlock()[0]==integers[0]&&ScoreBoardUtil.getColor(teamname).getBlock()[1]==integers[1]){
					this.type=ScoreBoardUtil.getColor(teamname).getBlock();
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkEnemyTeamBlock(Integer[] block){
		String teamname = ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),player).getName();
		for(String color:ScoreBoardUtil.colors){
			if(color.equals(teamname)){
				continue;
			}
			if(ScoreBoardUtil.getColor(color).getBlock()[0]==block[0]&&ScoreBoardUtil.getColor(color).getBlock()[1]==block[1]){
				this.type=ScoreBoardUtil.getColor(teamname).getBlock();
				return true;
			}
		}
		return false;
	}

	public boolean isNoDamage(){
		if(NoDamageTick>0){
			return true;
		}else{
			return false;
		}
	}
	public void setNoDamageTick(int i) {
		this.NoDamageTick=i;
	}
	public void setMuteki(int i) {
		this.muteki = i;
	}
	public boolean isJumping() {
		if(jumping>0){
			return true;
		}else{
			return false;
		}
	}
	public boolean isMuteki(){
		if(muteki>0){
			return true;
		}else{
			return false;
		}
	}

	public boolean isSpectator(){
		return spectator;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isIka() {
		return isIka;
	}

	public void onJump() {
		jumping = 20;
	}
	public void setSpectator(boolean b) {
		if(b){
			this.spectator = true;
			setMuteki(2);
			if(Splatoon.ikaConfig.spectatorMode){
				player.setGameMode(GameMode.SPECTATOR);
			}else{
				setHeight(player,0F,0F,0F);
				onINVISIBILITY(true);
				player.setAllowFlight(true);
				player.setFlying(true);
			}
		}else{
			this.spectator = false;
			if(Splatoon.ikaConfig.spectatorMode){
				if(player.getGameMode()==GameMode.SPECTATOR){
					player.setGameMode(GameMode.SURVIVAL);
				}
			}else{
				setHeight(player,0F,0.6F,1.8F);
				onINVISIBILITY(false);
				if(player.getGameMode()==GameMode.CREATIVE){
					return;
				}
				player.setAllowFlight(false);
				player.setFlying(false);
			}
		}
	}

	public void setLength(){
		if(isSpectator()){
			return;
		}
		if(Splatoon.ikaConfig.spectatorMode){
			//1.8の対応はまだ。

			return;
		}
		if(swim){
			setHeight(player,0F,0.6F,Splatoon.ikaConfig.getSwimLength());
		}else{
			setHeight(player,0F,0.6F,1.8F);
		}
	}

	/**
	 * 1.8のスコアボード。透明化してもプレイヤーのタグが見えるバグ修正用
	 * @param b
	 */
	public void onINVISIBILITY(boolean b){
		if(b){
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,9999*20,0));
			Team team = ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(), player);
			if(team!=null){
				for(Player send:Splatoon.getOnlinePlayers()){
					if(send==player){
						continue;
					}
					if(isSpectator()){//全員に送信
						ScoreBoardUtil.leavePlayerTeam(send.getScoreboard(),player);
					}else{//敵のプレイヤーに送信
						Team sendteam = ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(),send);
						if(sendteam!=null&&sendteam.getName().equals(team.getName())){
							continue;
						}
						ScoreBoardUtil.leavePlayerTeam(send.getScoreboard(),player);
					}
				}
			}
		}else{
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			Team team = ScoreBoardUtil.getPlayerTeam(ScoreBoardUtil.getMainScoreboard(), player);
			if(team!=null){
				for(Player send:Splatoon.getOnlinePlayers()){
					if(send==player){
						continue;
					}
					ScoreBoardUtil.addPlayerTeam(send.getScoreboard(),player,team.getName());
				}
			}
		}
	}

	/**
	 * 判定変更。デフォルト:0.0F, 0.6F, 1.8F
	 * @param p
	 * @param height
	 * @param width
	 * @param length
	 */
	public void setHeight(Player p, float height, float width, float length){
		try{
			Method handle = p.getClass().getMethod("getHandle");
			Class<?> c = Class.forName(Splatoon.ikaConfig.Version + ".Entity");
			Field heightfield = c.getDeclaredField("height");
			Field widthfield = c.getDeclaredField("width");
			Field lengthfield = c.getDeclaredField("length");
			heightfield.setFloat(handle.invoke(p), height);
			widthfield.setFloat(handle.invoke(p), width);
			lengthfield.setFloat(handle.invoke(p), length);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void onClass() {
		SelectClass.setInventry(player);
		isHave = true;
	}

	public void setClass(IkaClass class1) {
		this.SelectClass = class1;
		onClass();
	}
}
