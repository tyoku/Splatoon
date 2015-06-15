package com.github.kotake545.splatoon;

import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;

public class ProjectileInfo {
	private Projectile projectile;
	private Vector vector;
	private Location startloc;
	private Location lastloc;
	private IkaPlayerInfo shooter;
	private IkaWeapon weapon;
	private Integer[] block;

	private int tick;
	private boolean dead = false;
	private boolean canmove = true;

	public ProjectileInfo(IkaPlayerInfo shooter,IkaWeapon weapon,Vector vec) {
		this.shooter = shooter;
		this.weapon = weapon;
		this.vector = vec;
		this.block = shooter.getTeamBlock();
		this.projectile = shooter.getPlayer().launchProjectile(Snowball.class);
		projectile.setShooter(shooter.getPlayer());
		startloc = projectile.getLocation();
	}

	public void tick(){
		if(!dead&&!projectile.isDead()){
			tick++;
			if(projectile != null){
				lastloc = projectile.getLocation();
				if(block!=null){
					ParticleAPI.sendAllPlayer(EnumParticle.BLOCK_DUST,lastloc,0,0,0,0,1);
				}
				if(canmove){
					if(startloc.distance(lastloc)>weapon.getShootDistance()){
						canmove = false;
						vector.multiply(0.25D);
					}
					projectile.setVelocity(vector);
				}
			}else{
				dead = true;
			}
		}else{
			dead=true;
		}
		if(dead){
			remove();
		}
	}
	public void remove(){
		onHit();
		Splatoon.projectileManager.remove(this);
		onDestroy();
	}
	public void onHit(){
		double radius = weapon.getHitRadius();
		//まだ
	}
	public void onDestroy(){
		projectile.remove();
		projectile = null;
		vector = null;
		weapon = null;
		shooter = null;
	}
}
