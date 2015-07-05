package com.github.kotake545.splatoon.listener;

import org.bukkit.Location;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.ProjectileInfo;
import com.github.kotake545.splatoon.Splatoon;

public class projectileListener implements Listener{
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event){
		ProjectileInfo pInfo = Splatoon.projectileManager.getProjectileInfo(event.getEntity());
		if(pInfo==null){
			return;
		}
		Projectile projectile = pInfo.getProjectile();
		Location hit=pInfo.getProjectile().getLocation();
		for(double i = 0.2D; i < 4.0D; i += 0.2D){
			if(hit.getBlock().getTypeId() == 0){
				hit = hit.add(projectile.getVelocity().normalize().multiply(i));
			}
		}
		pInfo.onHit(hit);
		pInfo.setDestroyNextTick(true);
		event.getEntity().remove();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileDamage(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Projectile){
			ProjectileInfo pi=Splatoon.projectileManager.getProjectileInfo((Projectile) event.getDamager());
			if(pi!=null){
				event.setDamage(pi.getDamage());
				Location from = event.getEntity().getLocation();
				for(double i = 0.2D; i < 4.0D; i += 0.2D){
					if(from.getBlock().getTypeId() == 0){
						Vector vector = pi.getProjectile().getVelocity();
						vector.add(new Vector(0,-1,0));
						from = from.add(vector.normalize().multiply(i));
					}
				}
				pi.onHit(from);
				pi.setDestroyNextTick(true);
				event.getDamager().remove();
			}
		}

	}
}
