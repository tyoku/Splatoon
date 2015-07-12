package com.github.kotake545.splatoon.Manager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.github.kotake545.splatoon.IkaClass;
import com.github.kotake545.splatoon.Splatoon;

public class IkaClassManager {
	private static List<IkaClass> ikaClass;

	public IkaClassManager(){
		ikaClass= new ArrayList<IkaClass>();
	}

	public boolean loadIkaClass(){
		String path = Splatoon.getPluginFolder() + "/class";
		File dir = new File(path);
		String[] list = dir.list();
		if(list != null){
			for(String filename: list){
				IkaClass ic = loadClassforYml(new File(path + "/" + filename));
				ikaClass.add(ic);
			}
		}
		return true;
	}
	public IkaClass loadClassforYml(File file){
		IkaClass ic = new IkaClass();
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
			loadData(ic,string);
		}
		return ic;
	}

	private void loadData(IkaClass ikaclass, String string) {
		try {
			if(string.indexOf("=") <= 0){
				return;
			}
			String load = string.substring(0,string.indexOf("=")).toLowerCase();
			String set = string.substring(string.indexOf("=") + 1);
			if(load.equals("classname")){
				ikaclass.className=set;
			}
			if(load.equals("mainweapon")){
				ikaclass.mainWeapon=set;
			}
			if(load.equals("subweapon")){
				ikaclass.subWeapon=set;
			}
			if(load.equals("specialweapon")){
				ikaclass.specialWeapon=set;
			}
		} catch (Exception e) {

		}
	}

	public List<IkaClass> getAllClass(){
		ArrayList<IkaClass> clones = new ArrayList<IkaClass>();
		for(int i = ikaClass.size() - 1; i >= 0; i--){
			clones.add(ikaClass.get(i).clone());
		}
		return clones;
	}

	public IkaClass getClass(String itemDisplayName) {
		for(int i = ikaClass.size() - 1; i >= 0; i--){
			String name = ChatColor.stripColor(ikaClass.get(i).getName());
			if(name.toLowerCase().equals(itemDisplayName.toLowerCase())){
				return ikaClass.get(i);
			}
		}
		return null;
	}
}
