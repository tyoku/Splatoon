package com.github.kotake545.splatoon;

import org.bukkit.entity.Player;


public class IkaClass {
	public String className; //クラス名
	public String mainWeapon; //メインウェポン
	public String subWeapon; //サブウェポン
	public String specialWeapon; //スペシャルウェポン

	public IkaClass clone(){
		IkaClass clone = new IkaClass();
		clone.className = this.className;
		clone.mainWeapon = this.mainWeapon;
		clone.subWeapon = this.subWeapon;
		clone.specialWeapon = this.specialWeapon;
		return clone;
	}

	public IkaClass(){
		this.className = "";
		this.mainWeapon = "";
		this.subWeapon = "";
		this.specialWeapon = "";
	}

	public String getName() {
		return className;
	}

	public void setInventry(Player player) {
		//とりあえず装備を全部消す
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);

		//アイテムスロットに追加。
		IkaWeapon main = Splatoon.ikaWeaponManager.getWeapon(mainWeapon);
		if(main!=null){
			player.getInventory().setItem(0,main.getItemStack());
		}
		IkaWeapon sub = Splatoon.ikaWeaponManager.getWeapon(subWeapon);
		if(sub!=null){
			player.getInventory().setItem(1,sub.getItemStack());
		}
		IkaWeapon special = Splatoon.ikaWeaponManager.getWeapon(specialWeapon);
		if(sub!=null){
			player.getInventory().setItem(2,special.getItemStack());
		}

		player.updateInventory();
	}
}
