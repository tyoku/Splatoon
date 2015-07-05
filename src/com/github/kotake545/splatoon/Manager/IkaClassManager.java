package com.github.kotake545.splatoon.Manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.kotake545.splatoon.Splatoon;

public class IkaClassManager {
	private static Hashtable<String,List<List<ItemStack>>> ikaClass;

	public IkaClassManager(){
		ikaClass= new Hashtable<String, List<List<ItemStack>>>();
	}

	@SuppressWarnings("unchecked")
	public boolean loadIkaClass(){
		String path = Splatoon.getPluginFolder() + "/class";
		File dir = new File(path);
		String[] list = dir.list();
		if(list != null){
			for(String filename: list){
				String className = null;
				YamlConfiguration yml = Splatoon.getYml(new File(path + "/" + filename));
				Set<String> keys = yml.getConfigurationSection("").getKeys(false);
				for(String name:keys){
					className = name;
					break;
				}
				if(className!=null&&yml.get(String.format("%s",className))!=null){
					List<List<ItemStack>> inv = new ArrayList<List<ItemStack>>();
					inv = (List<List<ItemStack>>) yml.get(className);
					ikaClass.put(className, inv);
				}
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static void setInventry(Player player,List<List<ItemStack>> inv){
		PlayerInventory inventory = player.getInventory();
		inventory.clear();
		inventory.setHelmet(null);
		inventory.setChestplate(null);
		inventory.setLeggings(null);
		inventory.setBoots(null);
		int pos = 0;
		for(ItemStack a :inv.get(0)){
			 player.getInventory().setItem(pos,a);
			 pos++;
		}
		player.getInventory().setHelmet(inv.get(1).get(3));
		player.getInventory().setChestplate(inv.get(1).get(2));
		player.getInventory().setLeggings(inv.get(1).get(1));
		player.getInventory().setBoots(inv.get(1).get(0));
		player.updateInventory();
	}

	public List<List<ItemStack>> getClass(String className){
		if(ikaClass.containsKey(className.toLowerCase())){
			return ikaClass.get(className.toLowerCase());
		}
		return null;
	}
	public boolean addClass(String className,PlayerInventory inventory){
		String name = className.toLowerCase();
		if(ikaClass.containsKey(name)){
			return false;
		}else{
			String path = Splatoon.getPluginFolder() + "/class";
			File newfile = new File(path + "/" + name+".yml");
			try {
				newfile.createNewFile();
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
			YamlConfiguration yml = Splatoon.getYml(newfile);
			List<List<ItemStack>> inv = new ArrayList<List<ItemStack>>();
			List<ItemStack> contents = new ArrayList<ItemStack>();
			ItemStack[] i = inventory.getContents();
			int pos = 0;
			for (ItemStack stack : i) {
				contents.add(pos, stack);
				pos++;
				}
			inv.add(contents);
			int pos2 = 0;
			List<ItemStack> armorcontents = new ArrayList<ItemStack>();
			ItemStack[] a = inventory.getArmorContents();
			for (ItemStack stack : a) {
				armorcontents.add(pos2, stack);
				pos2++;
				}
			inv.add(armorcontents);
			yml.set(String.format("%s",name),inv);
			ikaClass.put(name, inv);
			try {
				yml.save(newfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public Set<String> getAllClass() {
		return ikaClass.keySet();
	}
}
