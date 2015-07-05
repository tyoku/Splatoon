package com.github.kotake545.splatoon.Manager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.github.kotake545.splatoon.IkaStage;
import com.github.kotake545.splatoon.Splatoon;

public class IkaStageManager {
	private static List<IkaStage> stages;

	public IkaStageManager(){
		stages= new ArrayList<IkaStage>();
	}

	public boolean loadIkaStages(){
		String path = Splatoon.getPluginFolder() + "/stages";
		File dir = new File(path);
		String[] list = dir.list();
		if(list != null){
			for(String filename: list){
				IkaStage ikaStage = loadYml(new File(path + "/" + filename));
				stages.add(ikaStage);
			}
		}
		return true;
	}
	public static void createNewStageYml(String stagename) throws IOException{
		String path = Splatoon.getPluginFolder() + "/stages";
		File newyml = new File(path + "/" + stagename+".yml");
		newyml.createNewFile();
		String n = System.getProperty("line.separator");
		FileWriter edit = new FileWriter(newyml, true);
		edit.write("stageName="+stagename+n);
		edit.close();
	}

	public static File getStageFile(String stagename){
		String path = Splatoon.getPluginFolder() + "/stages";
		File yml = new File(path + "/" + stagename+".yml");
		return yml;
	}

	public IkaStage loadYml(File file){
		IkaStage ikaStage = new IkaStage();
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
			loadData(ikaStage,string);
		}
		return ikaStage;
	}
	private void loadData(IkaStage ikaStage, String string) {
		try {
			if(string.indexOf("=") <= 0){
				return;
			}
			String load = string.substring(0,string.indexOf("=")).toLowerCase();
			String set = string.substring(string.indexOf("=") + 1);
			if(load.equals("stagename")){
				ikaStage.stageName=set;
			}
			if(load.contains("spawnpoint[")){
				ikaStage.setTeamAndSpawn(load,set);
			}
			if(load.equals("gamestart")){
				ikaStage.gameStart=Integer.parseInt(set);
			}
			if(load.equals("countdown")){
				ikaStage.countDown=Integer.parseInt(set);
			}
			if(load.equals("gametime")){
				ikaStage.gameTime=Integer.parseInt(set);
			}
		} catch (Exception e) {
		}
	}

	public void addStage(IkaStage ikaStage){
		stages.add(ikaStage);
	}

	public List<IkaStage> getAllStages(){
		return stages;
	}

	public IkaStage getStage(String stageName) {
		for(int i = stages.size() - 1; i >= 0; i--){
			String name = ChatColor.stripColor(stages.get(i).getName());
			if(name.toLowerCase().equals(stageName.toLowerCase())){
				return stages.get(i);
			}
		}
		return null;
	}
}
