package com.github.kotake545.splatoon.Manager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.github.kotake545.splatoon.IkaWeapon;
import com.github.kotake545.splatoon.Splatoon;

public class IkaWeaponManager {
	private static List<IkaWeapon> weapons;

	public IkaWeaponManager(){
		weapons= new ArrayList<IkaWeapon>();
	}

	public boolean loadIkaWeapons(){
		String path = Splatoon.getPluginFolder() + "/weapons";
		File dir = new File(path);
		String[] list = dir.list();
		if(list != null){
			for(String filename: list){
				IkaWeapon weapon = loadGunforYml(new File(path + "/" + filename));
				weapons.add(weapon);
			}
		}
		return true;
	}
	public IkaWeapon loadGunforYml(File file){
		IkaWeapon ikaWeapon = new IkaWeapon();
		ArrayList<String> line = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while((strLine = br.readLine()) != null){
				line.add(strLine);
			}
			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
		}
		for(String string:line){
			loadData(ikaWeapon,string);
		}
		return ikaWeapon;
	}
	private void loadData(IkaWeapon ikaWeapon, String string) {
		try {
			if(string.indexOf("=") <= 0){
				return;
			}
			String load = string.substring(0,string.indexOf("=")).toLowerCase();
			String set = string.substring(string.indexOf("=") + 1);
			if(load.equals("weaponname")){
				ikaWeapon.weaponName=set;
			}
			if(load.equals("weaponid")){
				ikaWeapon.setWeaponID(set);
			}
			if(load.equals("weapontype")){
				ikaWeapon.weaponType=set;
			}
			if(load.equals("rightclickevent")){
				ikaWeapon.setRightClick(set.toLowerCase());
			}
			if(load.equals("leftclickevent")){
				ikaWeapon.setLeftClick(set.toLowerCase());
			}
			if(load.equals("miningfatigue")){
				ikaWeapon.miningFatigue=Integer.parseInt(set);
			}
			if(load.equals("needink")){
				ikaWeapon.needInk=Integer.parseInt(set);
			}
			if(load.equals("distance")){
				ikaWeapon.distance=Float.parseFloat(set);
			}
			if(load.equals("usedelay")){
				ikaWeapon.useDelay=Integer.parseInt(set);
			}
			if(load.equals("addmovespeed")){
				ikaWeapon.addMoveSpeed=Float.parseFloat(set);
			}
			if(load.equals("damage")){
				ikaWeapon.damage=Double.parseDouble(set);
			}
			if(load.equals("sound")){
				ikaWeapon.setSound(set);
			}
			if(load.equals("shootspeed")){
				ikaWeapon.shootSpeed=Double.parseDouble(set);
			}
			if(load.equals("shootneedink")){
				ikaWeapon.shootNeedInk=Integer.parseInt(set);
			}
			if(load.equals("hitradius")){
				ikaWeapon.hitRadius=Double.parseDouble(set);
			}
			if(load.equals("movefinishmultiply")){
				ikaWeapon.moveFinishMultiply=Double.parseDouble(set);
			}
			if(load.equals("shootdelay")){
				ikaWeapon.shootDelay=Integer.parseInt(set);
			}
			if(load.equals("shootdistance")){
				ikaWeapon.shootDistance=Integer.parseInt(set);
			}
			if(load.equals("shootdamage")){
				ikaWeapon.shootDamage=Double.parseDouble(set);
			}
			if(load.equals("accuracy")){
				ikaWeapon.accuracy=Double.parseDouble(set);
			}
			if(load.equals("canscope")){
				ikaWeapon.canScope=Integer.parseInt(set);
			}
			if(load.equals("shootburst")){
				ikaWeapon.shootBurst=Integer.parseInt(set);
			}
			if(load.equals("shootsound")){
				ikaWeapon.setShootSound(set);
			}
			if(load.equals("fullauto")){
				ikaWeapon.fullAuto=Boolean.parseBoolean(set);
			}
			if(load.equals("disablefirstuse")){
				ikaWeapon.DisableFirstUse=Boolean.parseBoolean(set);
			}
			if(load.equals("shootreaction")){
				ikaWeapon.shootReaction=Double.parseDouble(set);
			}
			if(load.equals("shootrecoil")){
				ikaWeapon.shootRecoil=Double.parseDouble(set);
			}
			if(load.equals("shootknockback")){
				ikaWeapon.shootKnockback=Double.parseDouble(set);
			}
			if(load.equals("chargetick")){
				ikaWeapon.chargeTick=Integer.parseInt(set);
			}
			if(load.equals("ballistic")){
				ikaWeapon.ballistic=set.toUpperCase();
			}
			if(load.equals("paintballistic")){
				ikaWeapon.paintBallistic=Boolean.parseBoolean(set);
			}
			if(load.equals("paintballisticheight")){
				ikaWeapon.paintBallisticHeight=Integer.parseInt(set);
			}
			if(load.equals("paintballisticdistance")){
				ikaWeapon.paintBallisticDistance=Float.parseFloat(set);
			}
		} catch (Exception e) {

		}
	}

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
		if(name.indexOf(" [") <= 0){
			return name;
		}
		name = name.substring(0,name.indexOf(" ["));
		return name;
	}

	public List<IkaWeapon> getAllWeapons(){
		ArrayList<IkaWeapon> clones = new ArrayList<IkaWeapon>();
		for(int i = weapons.size() - 1; i >= 0; i--){
			clones.add(weapons.get(i).clone());
		}
		return clones;
	}

	public IkaWeapon getWeapon(String itemDisplayName) {
		for(int i = weapons.size() - 1; i >= 0; i--){
			String name = ChatColor.stripColor(weapons.get(i).getName());
			if(name.toLowerCase().equals(itemDisplayName.toLowerCase())){
				return weapons.get(i);
			}
		}
		return null;
	}

}
