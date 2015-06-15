package com.github.kotake545.splatoon;

import java.util.Hashtable;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.util.Utils;


public class IkaWeapon {
	private String weaponName;
	private Integer[] weaponID;
	private String weaponType;

	private String rightClick;
	private String leftClick;

	private int needInk;
	private int distance; //塗る範囲 長さ
	private int useDelay; //使える間隔
	private float addMoveSpeed;
	private double damage;
	private Hashtable<String,Integer[]> sound = new Hashtable<String, Integer[]>();

	private double shootSpeed;
	private int shootNeedInk;
	private int shootDistance;
	private int hitRadius;
	private int shootDelay;
	private int shootDamage;
	private double accuracy;
	private int canScope;
	private int shootBurst;
	private Hashtable<String,Integer[]> shootSound = new Hashtable<String, Integer[]>();
	private boolean fullAuto;

	private int ticks;
	private IkaPlayerInfo ika;
	private String lastClickType = "";


	public void shoot(){
		Player ikaPlayer = ika.getPlayer();
		if(ika!=null&&ikaPlayer.isOnline()){
			Location loc = ikaPlayer.getLocation();
			if(checkInk(shootNeedInk)){
				useInk(shootNeedInk);
				for(String name:shootSound.keySet()){
					Sound sound = Utils.getSound(name);
					Integer[] option = shootSound.get(name);
					if(sound != null){
						loc.getWorld().playSound(loc, sound,option[0],option[1]);
					}else{
						//なかった時、自前のサウンドをチェックして再生できるなら再生する。
					}
				}
				for(int i = 0; i<shootBurst; i++){
					Random rand = new Random();
					double dir = -loc.getYaw() - 90.0F;
					double pitch = -loc.getPitch();
					double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + getRandomAccuracy(rand);
					double yd = Math.sin(Math.toRadians(pitch)) + getRandomAccuracy(rand);
					double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + getRandomAccuracy(rand);
					Vector vec = new Vector(xd, yd, zd);
					vec.multiply(shootSpeed);
//					ProjectileInfo pi = new ProjectileInfo();
//					Splatoon.projectileManager.addProjectileInfo(pi);
					//まだ
				}
			}else{//インク不足
				ikaPlayer.playSound(loc, Sound.CLICK, 1.0F, 2.0F);
			}
		}
	}

	public double getRandomAccuracy(Random rand){
		double r = 1000.0D;
		int a = (int)(accuracy * r);
		if(a<=0){
			a = 1;
		}
		double randaccuracy = (rand.nextInt(a) - rand.nextInt() + 0.5D) / r;
		return randaccuracy;
	}

	public void tick() {
		if(ika!=null){
			//まだ
			//Delayの更新とかshootとかここで管理

		}

	}

	public boolean useInk(int need){
		if(checkInk(need)){
			ika.inkGauge-=need;
			return true;
		}else{
			return false;
		}
	}

	public boolean checkInk(int need){
		if(ika.inkGauge>=need){
			return true;
		}else{
			return false;
		}
	}

	public int getShootDistance(){
		return shootDistance;
	}

	public int getHitRadius() {
		return hitRadius;
	}
}
