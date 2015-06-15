package com.github.kotake545.splatoon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.kotake545.splatoon.Manager.IkaManager;
import com.github.kotake545.splatoon.Manager.IkaWeaponManager;
import com.github.kotake545.splatoon.Manager.ProjectileManager;
import com.github.kotake545.splatoon.listener.ikaListener;
import com.github.kotake545.splatoon.util.BlockUtil;



public class Splatoon extends JavaPlugin implements Listener {
	public static Logger logger;
	public static Splatoon instance;
	public static ikaConfig ikaConfig;
	public static IkaManager ikaManager;
	public static BlockUtil blockUtil;
	public static IkaWeaponManager ikaWeaponManager;
	public static ProjectileManager projectileManager;

 	public void onEnable(){
 		logger = this.getLogger();
 		instance = this;
 		ikaConfig = new ikaConfig();
 		ikaManager = new IkaManager();
 		ikaManager.reload();
 		blockUtil = new BlockUtil();
 		ikaWeaponManager = new IkaWeaponManager();
 		projectileManager = new ProjectileManager();

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new ikaListener(), this);

		Task();
//		getCommand("test").setExecutor(new TestCommand());
	}

	public void onDisable() {
		blockUtil.returnBeforeBlocks();
	}

	public void Task(){
		Bukkit.getScheduler().runTaskTimer(instance, new Runnable(){
			public void run(){
				ikaManager.tick();
			}
		}, 1L, 1L);
	}
    public static YamlConfiguration getConfiguration(String configName) {
        File file = new File(
                Splatoon.instance.getDataFolder() + File.separator + configName + ".yml");
        if ( !file.exists() ) {
            YamlConfiguration conf = new YamlConfiguration();
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }
    public static File getFile(String yml){
    	File file;
		file = new File(Splatoon.instance.getDataFolder() + File.separator +yml +".yml");
		if (!file.exists()) {
			YamlConfiguration conf = new YamlConfiguration();
			try {
				conf.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
    }

	public static Splatoon getInstance(){
		return instance;
	}

	public static void setCheckMovement(Player p,boolean check) throws Exception{
		Method PlayerHandle = p.getClass().getMethod("getHandle");
		Object EntityPlayer = PlayerHandle.invoke(p);
		Object PlayerConnection = EntityPlayer.getClass().getField("playerConnection").get(EntityPlayer);
		Field CheckMovement = PlayerConnection.getClass().getField("checkMovement");
		CheckMovement.setAccessible(check);
	}

	public static Player[] getOnlinePlayers(){
		try{
			return Bukkit.getOnlinePlayers().toArray(new Player[0]);
		}catch(NoSuchMethodError e){
			try{
				Method getOnlinePlayers = null;
				for(Method m : Bukkit.class.getDeclaredMethods()){
					if(m.getName().equals("getOnlinePlayers")){
						getOnlinePlayers = m;
						break;
					}
				}
				if(getOnlinePlayers == null) return new Player[]{};
				Object OnlinePlayers = getOnlinePlayers.invoke(Bukkit.class, new Object[0]);
				if(OnlinePlayers instanceof Player[]) return (Player[])OnlinePlayers;
			}catch (Exception es){}
			return new Player[]{};
		}
	}
}