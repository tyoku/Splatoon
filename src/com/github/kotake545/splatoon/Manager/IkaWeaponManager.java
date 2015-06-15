package com.github.kotake545.splatoon.Manager;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.github.kotake545.splatoon.IkaWeapon;

public class IkaWeaponManager {

	public static String getItemDisplayName(ItemStack item){
		if(item==null||item.getTypeId()==0){
			return "";
		}
		return getItemDisplayName(item.getItemMeta().getDisplayName());
	}

	public static String getItemDisplayName(String displayname){
		if(displayname==null){
			return "";
		}
		String name = ChatColor.stripColor(displayname);
		return name;
	}

	public IkaWeapon getWeapon(String itemDisplayName) {


		//まだ
		return null;
	}

}
