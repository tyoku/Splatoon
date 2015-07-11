package com.github.kotake545.splatoon.util;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.IkaPlayerInfo;
import com.github.kotake545.splatoon.Splatoon;

public class Utils {

	public static boolean isNumber(String val) {
		if(val.matches("^[0-9]{1,}$")){
			return true;
		}else{
			return false;
		}
	}
	public static Integer[] getBlock(Location loc){
		Integer[] b = new Integer[]{0,0};
		Block block = loc.getBlock();
		b[0] = block.getTypeId();
		b[1] = (int) block.getData();
		if(Splatoon.ikaConfig.blockset){
			return b;
		}else{
			if(Splatoon.blockUtil.getBlock(block)!=null){
				return Splatoon.blockUtil.getBlock(block);
			}else{
				return b;
			}
		}
	}

	public static void setEXPBar(Player player,double max,double ink){
		player.setLevel((int) ink);
		double pa = 1/max;
		player.setExp((float) (ink*pa));
	}
	public static Sound getSound(String soundName){
		Sound sound = Sound.valueOf(soundName.toUpperCase().replace(" ", "_"));
		return sound;
	}

	public static void addHealth(Player player,double heal){
		double health = player.getHealth()+heal;
		if(player.getMaxHealth()<=health){
			health=player.getMaxHealth();
		}
		player.setHealth(health);
	}

	public static void playSound(String name,Integer[] option,Location loc){
		Sound sound = Utils.getSound(name);
		if(sound != null){
			loc.getWorld().playSound(loc, sound,option[0],option[1]);
		}else{
			//なかった時、自前のサウンドをチェックして再生できるなら再生する。
		}
	}

	public static void Respawn(Player player){
		/**
		 * 最後に与えられたダメージ初期化。
		 */
		EntityDamageEvent ede = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.SUICIDE,0);
//		Bukkit.getPluginManager().callEvent(ede);
//		if (ede.isCancelled()) return;
		ede.getEntity().setLastDamageCause(ede);
		Location loc = ScoreBoardUtil.getSpawnLocation(player);
		IkaPlayerInfo ika = Splatoon.ikaManager.getIka(player);
		ika.setSpectator(false);
		Utils.heal(player);
		ika.Reset();
		ika.setMuteki(20*5);
		ika.onClass();
		player.teleport(loc,TeleportCause.PLUGIN);
	}

	public static void damage(EntityDamageEvent event, double d){
		if(event instanceof EntityDamageByEntityEvent){
			event.getEntity().setLastDamageCause((EntityDamageByEntityEvent)event);
		}
		double heal = 0;
		double damage = event.getDamage()-heal;
		double healsh = ((Damageable)((LivingEntity)event.getEntity())).getHealth()-damage;
		if(healsh<0.0001)healsh=0.0001;
		if(healsh>((Damageable)event.getEntity()).getMaxHealth())healsh = ((Damageable)event.getEntity()).getMaxHealth();
		((LivingEntity)event.getEntity()).setHealth(healsh);
	}
	public static void heal(final Player player) {
		if(player==null||!player.isOnline()){
			return;
		}
		player.setVelocity(new Vector());
		player.setFireTicks(0);
		player.setFallDistance(0);
		player.setHealth(((Damageable)player).getMaxHealth());
		player.setFoodLevel(2);
		player.setRemainingAir(player.getMaximumAir());
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		for ( PotionEffect e : effects ) {
			player.removePotionEffect(e.getType());
		}
        new BukkitRunnable() {
            public void run() {
            	player.setFireTicks(0);
            }
        }.runTaskLater(Splatoon.instance, 1L);
	}
}
