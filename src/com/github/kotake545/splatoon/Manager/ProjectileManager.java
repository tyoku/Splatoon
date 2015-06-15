package com.github.kotake545.splatoon.Manager;

import java.util.ArrayList;
import java.util.List;

import com.github.kotake545.splatoon.ProjectileInfo;

//IkaWeaponから飛ばしたインク(雪球)はここで管理してく
//void tick() 作ってSplatoonのスケジューラに入れてvectorとか管理してく
public class ProjectileManager {
	private static List<ProjectileInfo> projectiles;

	public ProjectileManager(){
		projectiles = new ArrayList<ProjectileInfo>();
	}
	
	public void remove(ProjectileInfo projectileInfo) {
//		if(){
//
//		}
		//まだ
	}

}
