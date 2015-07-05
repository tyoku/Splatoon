package com.github.kotake545.splatoon;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.kotake545.splatoon.Manager.IkaClassManager;
import com.github.kotake545.splatoon.Manager.IkaWeaponManager;
import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;
import com.github.kotake545.splatoon.util.ScoreBoardUtil;
import com.github.kotake545.splatoon.util.Utils;

public class IkaPlayerInfo {
	public Player player;
	public String name;
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

	public String SelectClassName;

	private Squid kagemusya=null;

	public IkaPlayerInfo(Player player) {
		this.player = player;
		this.name = ChatColor.stripColor(player.getName());
		this.isIka = false;
		this.NoDamageTick = 0;
		this.muteki = 0;
		this.canFly = false;
		this.swim = false;
		this.jumping = 0;
		this.tick = 0;
		this.returndelay = 0;
		this.type = new Integer[]{0,0};
		this.inkGauge = Splatoon.ikaConfig.inkGauge;
		this.weapon = null;
		this.lasthandItem = player.getItemInHand();
		player.setFoodLevel(2);
		this.clickType = "none";
		this.SelectClassName = Splatoon.ikaConfig.defaultClass;

		this.weapons = Splatoon.ikaWeaponManager.getAllWeapons();
		for(int i = 0; i < weapons.size(); i++){
			weapons.get(i).ika = this;
		}
	}

	/**
	 * 初期化
	 */
	public void Reset(){
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
		if(ScoreBoardUtil.getPlayerTeam(player)==null){
			if(canFly){
				player.setAllowFlight(true);
				player.setFlying(true);
			}else{
				player.setAllowFlight(false);
				player.setFlying(false);
			}
			player.setWalkSpeed(Splatoon.ikaConfig.moveSpeed);
			return;
		}
		if(Splatoon.MainTask.getGameStatus().equals("countdown")){
			return;
		}
		if(player.getLocation().clone().add(0,0.1,0).getBlock().getType().getId()==9){
			setNoDamageTick(0);
			player.damage(player.getMaxHealth());
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
		if(isIka){
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,9999,0));
			if(swim){
				if(lastSwim){
					//チャポン
					player.getLocation().getWorld().playSound(player.getLocation(),Sound.SWIM,(float)1,2);
					lastSwim=false;
				}
				if(tick%Splatoon.ikaConfig.inkHealSpeed==0&&inkGauge<Splatoon.ikaConfig.inkGauge){
					inkGauge+=Splatoon.ikaConfig.inkHeal;
					if(inkGauge>Splatoon.ikaConfig.inkGauge){
						inkGauge=Splatoon.ikaConfig.inkGauge;
					}
				}
				ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_CRACK.setItemIDandData(this.type[0],this.type[1]),player.getLocation().clone().add(0,0,0),0.0F,0F,0.0F,0,10);
				lastspeed =  Splatoon.ikaConfig.ikaSpeed;
			}else{
				//潜ってない時はイカの姿が現れる。
				//LibsDisguiseかなんかでいいかなんて思ってます。
				if(!lastSwim){
					player.getLocation().getWorld().playSound(player.getLocation(),Sound.SWIM,(float)1,1);
					lastSwim=true;
				}
				//潜ってから飛んでる間はspeedは変わらない。
				if(player.isOnGround()){
					lastspeed = 0.01F;
				}
			}
		}else{
			if(!lastSwim){
				player.getLocation().getWorld().playSound(player.getLocation(),Sound.SWIM,(float)1,1);
				lastSwim=true;
			}
			lastspeed = Splatoon.ikaConfig.moveSpeed;
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			if(player.isOnGround()){
				Integer[] block = Utils.getBlock(player.getLocation().clone().add(0,-0.1,0));
				if(checkEnemyTeamBlock(block)){
					lastspeed = Splatoon.ikaConfig.downSpeed;
					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,9999,0));
					poisonFlag=true;
				}
			}
		}
		if(!poisonFlag){
			player.removePotionEffect(PotionEffectType.POISON);
		}
		if(isIka&&player.isSneaking()){
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,0,4));
		}else{
			player.removePotionEffect(PotionEffectType.SPEED);
		}
		if(player.getWalkSpeed()!=lastspeed){
			player.setWalkSpeed(lastspeed);
		}

		healTick();
		weaponTick();
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
			IkaWeapon iw = getWeapon(IkaWeaponManager.getItemDisplayName(hand));
			weapon = iw;
			if(iw == null||lasthandItem!=hand){
				player.removePotionEffect(PotionEffectType.SLOW);
				player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
			}
			if(iw!=null){
				if(iw.miningFatigue>0){
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,100,iw.miningFatigue-1));
				}
			}
			lasthandItem=hand;
		}else{
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
			if(weapons.get(i).getName().toLowerCase().equals(itemDisplayName)){
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
		if(isIka){
			isIka=false;
		}else{
			isIka=true;
		}
	}

	public void setClass(String className){
		this.SelectClassName = className;
		List<List<ItemStack>> inv = Splatoon.ikaClassManager.getClass(className);
		if(inv!=null){
			IkaClassManager.setInventry(player, inv);
		}else{
			inv = Splatoon.ikaClassManager.getClass(Splatoon.ikaConfig.defaultClass);
			if(inv!=null){
				IkaClassManager.setInventry(player,inv);
			}
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
		if(Splatoon.ikaConfig.downSteps){
			if(isIka&&player.isOnGround()&&!isJumping()&&
					player.getLocation().clone().add(0,-0.1,0).getBlock().getTypeId()==0
					&&player.getLocation().clone().add(0,-1.1,0).getBlock().getTypeId()!=0){
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
		if(ScoreBoardUtil.getPlayerTeam(player) != null){
			String teamname = ScoreBoardUtil.getPlayerTeam(player).getName();
			return ScoreBoardUtil.getColor(teamname).getBlock();
		}
		return null;
	}

	public boolean checkTeamBlock(Integer[] integers){
		if(integers!=null){
			if(ScoreBoardUtil.getPlayerTeam(player) != null){
				String teamname = ScoreBoardUtil.getPlayerTeam(player).getName();
				if(ScoreBoardUtil.getColor(teamname).getBlock()[0]==integers[0]&&ScoreBoardUtil.getColor(teamname).getBlock()[1]==integers[1]){
					this.type=ScoreBoardUtil.getColor(teamname).getBlock();
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkEnemyTeamBlock(Integer[] block){
		String teamname = ScoreBoardUtil.getPlayerTeam(player).getName();
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

	public Player getPlayer() {
		return player;
	}

	public boolean isIka() {
		return isIka;
	}

	public void onJump() {
		jumping = 10;
	}

	public void onClass() {
		setClass(SelectClassName);
	}
}
