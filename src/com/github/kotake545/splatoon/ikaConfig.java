package com.github.kotake545.splatoon;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.kotake545.splatoon.util.Utils;

public class ikaConfig {
	public String Version = null;

	public boolean ikaChangeforSneak = false; //イカチェンジ Drop か Sneak か
	public int spectatorTime = 5; //spectatorTime
	public boolean  spectatorMode = false;//

	public boolean changeSwimLength = true;
	public float isSwimLength = 0.1F;

	public boolean blockset = false; //ワールドには書き込まない
	public boolean particle = true; //パーティクルのON/OFF
	public boolean damageEffect = true; //エフェクト

	public float moveSpeed = 0.2F; //デフォルト移動速度
	public float ikaSpeed = 0.4F; //イカ移動速度 スニークで移動速度下がるから チェック時に2倍にして
	public float downSpeed = 0.1F; //敵のインクにいる時のスピード

	public int inkGauge = 50; //デフォルトのインクゲージ
	public int inkHealSpeed = 1; //インクの回復速度 低いほうが速いです
	public int inkHeal = 1; //インクの回復量

	public int healDelay = 50; //ダメージを喰らってから回復し始めるまでの待機時間
	public int healSpeed = 1; //体力の回復速度
	public float heal = 0.5F;//体力の回復量

	public boolean inkWool = true; //インクのタイプ 羊毛がいやならCLAYにしてください
	public List<Integer[]> inkCanselBlocks = new ArrayList<Integer[]>(); //インクを受け付けないブロック
	public List<Integer[]> fenceBlocks = new ArrayList<Integer[]>(); //フェンス

	public Hashtable<String,Integer[]> shootPaintSound=new Hashtable<String,Integer[]>();
	public Hashtable<String,Integer[]> usePaintSound=new Hashtable<String,Integer[]>();

	public boolean downSteps = true;

	public String defaultClass = "";

	public double deadInkRadius = 3.0D;

	public boolean changeColorGlass = false;

	public Location spawnLocation = null;

	public ikaConfig(){
		try {
			Object obj = Bukkit.getServer().getClass().getDeclaredMethod("getServer", new Class[0]).invoke(Bukkit.getServer(), new Object[0]);
			Object propertyManager = obj.getClass().getDeclaredMethod("getPropertyManager", new Class[0]).invoke(obj, new Object[0]);
			Version = propertyManager.getClass().getPackage().getName();
		} catch (Exception e){
			e.printStackTrace();
		}
        File configFile = new File(Splatoon.getInstance().getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
        	Splatoon.getInstance().saveDefaultConfig();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        spectatorMode = config.getBoolean("spectatorMode");
        spectatorTime = config.getInt("spectatorTime");
        deadInkRadius = config.getDouble("deadInkRadius");
        blockset = config.getBoolean("blockSet");
        particle = config.getBoolean("particle");
        moveSpeed = (float) config.getDouble("moveSpeed");
        ikaSpeed = (float) config.getDouble("ikaSpeed");
        downSpeed = (float) config.getDouble("downSpeed");
        inkGauge = config.getInt("inkGauge");
        inkHealSpeed = config.getInt("inkHealSpeed");
        inkHeal = config.getInt("inkHeal");
        healDelay = config.getInt("healDelay");
        healSpeed = config.getInt("healSpeed");
        heal = (float) config.getDouble("heal");
        defaultClass = config.getString("defaultClass");
        damageEffect = config.getBoolean("damageEffect");

        changeColorGlass = config.getBoolean("changeColorGlass");
        changeSwimLength = config.getBoolean("changeSwimLength");

        isSwimLength = (float) config.getDouble("isSwimLength");
        if(config.getString("inkType")!=null){
            if(config.getString("inkType").toUpperCase().equals("WOOL")){
            	inkWool = true;
            }else{
            	inkWool = false;
            }
        }
        if(config.getString("ikaChangeEvent")!=null){
            if(config.getString("ikaChangeEvent").toUpperCase().equals("SNEAK")){
            	ikaChangeforSneak = true;
            }else{
            	ikaChangeforSneak = false;
            }
        }
        if(config.getString("inkCanselBlocks")!=null){
            inkCanselBlocks = getInkCanselBlocks(config.getString("inkCanselBlocks"));
        }else{
        	inkCanselBlocks.add(new Integer[]{0,0});
        }
        shootPaintSound = IkaWeapon.getSoundData(config.getString("shootPaintSounds"));
        usePaintSound = IkaWeapon.getSoundData(config.getString("usePaintSounds"));
        downSteps = config.getBoolean("downSteps");

        //コンフィグを読み込むのと同じタイミングでロビーのスポーンポイントも読み込む。
        //ない場合はnullをセットしておく。
        File spawnYML = getSpawnYML();
		if(spawnYML!=null){
			spawnLocation = loadSpawn(spawnYML);
		}else{
			spawnLocation = null;
		}
	}

	public void createSpawnYML(Location location) throws IOException {
		String world=location.getWorld().getName();
		String x = String.valueOf(location.getX());
		String y = String.valueOf(location.getY());
		String z = String.valueOf(location.getZ());
		String yaw = String.valueOf(location.getYaw());
		String pitch = String.valueOf(location.getPitch());
		String loc = world+"/"+x+"/"+y+"/"+z+"/"+yaw+"/"+pitch;
		File spawnYML = getSpawnYML();
		String n = System.getProperty("line.separator");
		String path = Splatoon.getPluginFolder();
		if(spawnYML==null){
			File newyml = new File(path + "/" + "spawn.yml");
			newyml.createNewFile();
			FileWriter edit = new FileWriter(newyml, true);
			edit.write("spawnpoint="+loc);
			edit.close();
			return;
		}
		//編集
		IkaStage.onEditYml(spawnYML,"spawn",path,"spawnpoint",loc);
	}

	public static File getSpawnYML(){
		File spawnYML=null;
		String path = Splatoon.getPluginFolder();
		File dir = new File(path);
		String[] list = dir.list();
		if(list != null){
			for(String filename: list){
				if(filename.toLowerCase().equals("spawn.yml")){
					spawnYML = new File(path + "/" + filename);
				}
			}
		}
		return spawnYML;
	}
	public Location loadSpawn(File file){
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
			if(string.indexOf("=") <= 0){
				continue;
			}
			String load = string.substring(0,string.indexOf("=")).toLowerCase();
			if(load.equals("spawnpoint")){
				return IkaStage.getLocation(string.substring(string.indexOf("=") + 1));
			}
		}
		return null;
	}


	public List<Integer[]> getInkCanselBlocks(String string){
		List<Integer[]> blocks = new ArrayList<Integer[]>();
		blocks.add(new Integer[]{0,0});
		for(String i:string.split(",")){
			Integer[] ids = new Integer[]{0,0};
			if(Utils.isNumber(i)){
				ids[0]=Integer.valueOf(i);
				ids[1]=-1;
				blocks.add(ids);
				continue;
			}else{
				if(i.split(":").length >= 2){
					if(Utils.isNumber(i.split(":")[0])){
						ids[0]=Integer.valueOf(i.split(":")[0]);
					}else{
						Splatoon.logger.info("[Config読み込みエラー] "+"inkcanselblocks: "+i.split(":")[0]+" が数字ではありません。");
						continue;
					}
					if(Utils.isNumber(i.split(":")[1])){
						ids[1]=Integer.valueOf(i.split(":")[1]);
					}else{
						Splatoon.logger.info("[Config読み込みエラー] "+"inkcanselblocks: "+i.split(":")[1]+" が数字ではありません。");
						continue;
					}
					blocks.add(ids);
					continue;
				}else{
					Splatoon.logger.info("[Config読み込みエラー] "+"inkcanselblocks: "+i+" の指定が間違えています。ID:subID");
					continue;
				}
			}
		}
		return blocks;
	}

	public float getSwimLength() {
		if(!Splatoon.ikaConfig.changeSwimLength){
			return 1.8F;
		}
		return isSwimLength;
	}
}
