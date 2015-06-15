package com.github.kotake545.splatoon;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
	public int inkGauge = 0;
	public Integer[] type = new Integer[]{0,0};

	public int tick = 0;
	public int returndelay = 0;
	public int jumping = 0;

	public IkaWeapon weapon;
	public List<IkaWeapon> weapons;
	public ItemStack lasthandItem;
	public String clickType;

	public IkaPlayerInfo(Player player) {
		this.player = player;
//		Splatoon.setCheckMovement(player,false);
		ScoreBoardUtil.addPlayerTeam(player,"red");
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
		this.weapons = new ArrayList<IkaWeapon>();
		this.lasthandItem = player.getItemInHand();
		this.clickType = "none";
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
		float speed = Splatoon.ikaConfig.moveSpeed;
		player.removePotionEffect(PotionEffectType.POISON);
		if(player.getGameMode()==GameMode.CREATIVE){
			player.setWalkSpeed(speed);
			return;
		}
		onIka();
		if(canFly){
			player.setAllowFlight(true);
			player.setFlying(true);
		}else{
			player.setAllowFlight(false);
			player.setFlying(false);
		}
		if(isIka){
			if(swim){
				if(tick%Splatoon.ikaConfig.inkHealSpeed==0&&inkGauge<Splatoon.ikaConfig.inkGauge){
					inkGauge+=Splatoon.ikaConfig.inkHeal;
					if(inkGauge>Splatoon.ikaConfig.inkGauge){
						inkGauge=Splatoon.ikaConfig.inkGauge;
					}
				}
				ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_CRACK.setItemIDandData(this.type[0],this.type[1]),player.getLocation().clone().add(0,0,0),0.0F,0F,0.0F,0,10);
			}
			speed =  Splatoon.ikaConfig.ikaSpeed;
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,9999,0));
			//イカの時は行動不能
		}else{
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			if(player.isOnGround()){
				Integer[] block = Utils.getBlock(player.getLocation().clone().add(0,-0.1,0));
				if(checkEnemyTeamBlock(block)){
					speed = Splatoon.ikaConfig.downSpeed;
					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,9999,0));
				}
			}
		}
		if(isIka&&player.isSneaking()){
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,9999,4));
		}else{
			player.removePotionEffect(PotionEffectType.SPEED);
		}
		if(player.getWalkSpeed()!=speed){
			player.setWalkSpeed(speed);
		}

		weaponTick();

		ReflashBlock();
		ReflashInk();
		downstep();
	}

	public void weaponTick(){
		ItemStack hand = player.getItemInHand();
		if(hand != null){
			if(tick % 10 == 0){
				IkaWeapon iw = Splatoon.ikaWeaponManager.getWeapon(IkaWeaponManager.getItemDisplayName(hand));
				weapon = iw;
				if(iw == null||lasthandItem!=hand){
					player.removePotionEffect(PotionEffectType.SLOW);
				}
			}
		}else{
			weapon = null;
			lasthandItem = null;
			player.removePotionEffect(PotionEffectType.SLOW);
		}
		for(int i = weapons.size() - 1; i >= 0; i--){
			IkaWeapon iw = weapons.get(i);
			if(iw != null){
				iw.tick();
			}
		}


	}

	public boolean onClick(String clickType){
		if(weapon!=null){
			this.clickType = clickType;
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

	@SuppressWarnings("deprecation")
	public void onIka(){
		canFly = false;
		swim = false;
		/**
		 * イカ壁同化中...
		 */
		if(!player.isOnGround()&&isIka){
			if(checkWall()){
				canFly = true;
				swim = true;
				return;
			}
		}
		if(player.isSneaking()){
			Location loc = player.getLocation();
			if(player.isOnGround()){
				Integer[] block = Utils.getBlock(loc.clone().add(0,-0.1,0));
				if(checkTeamBlock(block)){
					isIka = true;
					swim = true;
					returndelay = 0;
				}else{
					if(returndelay>=14&&!isJumping()){
						isIka = false;
						return;
					}
					if(block[0]!=0&&Utils.getBlock(loc.clone())[0]==0){
						isIka = false;
					}
				}
			}else{
				if(checkWall()){
					isIka = true;
					canFly = true;
					swim = true;
				}else{
					canFly = false;
				}
			}
		}else{
			isIka = false;
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
}
