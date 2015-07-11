package com.github.kotake545.splatoon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.kotake545.splatoon.Manager.IkaClassManager;
import com.github.kotake545.splatoon.Manager.IkaManager;
import com.github.kotake545.splatoon.Manager.IkaStageManager;
import com.github.kotake545.splatoon.Manager.IkaWeaponManager;
import com.github.kotake545.splatoon.Manager.ProjectileManager;
import com.github.kotake545.splatoon.commands.IkaClassCommand;
import com.github.kotake545.splatoon.commands.IkaSplatoonCommand;
import com.github.kotake545.splatoon.commands.IkaStageCommand;
import com.github.kotake545.splatoon.commands.IkaWeaponCommand;
import com.github.kotake545.splatoon.listener.ikaListener;
import com.github.kotake545.splatoon.listener.projectileListener;
import com.github.kotake545.splatoon.util.BlockUtil;

public class Splatoon extends JavaPlugin implements Listener {
	public static Logger logger;
	public static Splatoon instance;
	public static ikaConfig ikaConfig;
	public static IkaManager ikaManager;
	public static BlockUtil blockUtil;
	public static IkaWeaponManager ikaWeaponManager;
	public static IkaStageManager ikaStageManager;
	public static ProjectileManager projectileManager;
	public static IkaClassManager ikaClassManager;
	public static Task MainTask;
	public static String format = ChatColor.AQUA+""+ChatColor.BOLD+"[Splatoon]"+ChatColor.RESET;

 	public void onEnable(){
 		logger = this.getLogger();
 		instance = this;
 		ikaConfig = new ikaConfig();
 		ikaManager = new IkaManager();
 		blockUtil = new BlockUtil();
 		ikaWeaponManager = new IkaWeaponManager();
 		ikaStageManager = new IkaStageManager();
 		projectileManager = new ProjectileManager();
 		ikaClassManager = new IkaClassManager();
 		MainTask = new Task();
//		removeAllSquid();

 		ikaClassManager.loadIkaClass();
 		ikaWeaponManager.loadIkaWeapons();
 		ikaStageManager.loadIkaStages();
 		ikaManager.reload();

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new ikaListener(), this);
		pm.registerEvents(new projectileListener(), this);

		Task();
		getCommand("ikaweapon").setExecutor(new IkaWeaponCommand());
		getCommand("splatoon").setExecutor(new IkaSplatoonCommand());
		getCommand("ikastage").setExecutor(new IkaStageCommand());
		getCommand("ikaclass").setExecutor(new IkaClassCommand());
	}

	public void onDisable() {
		blockUtil.returnBeforeBlocks();
//		removeAllSquid();
	}

	public void Task(){
		Bukkit.getScheduler().runTaskTimer(instance, new Runnable(){
			public void run(){
				MainTask.timer();
				ikaManager.tick();
				projectileManager.tick();
			}
		}, 1L, 1L);
	}

	public void removeAllSquid() {
		for ( World world : Bukkit.getWorlds() ) {
			for ( Entity entity : world.getEntities() ) {
				if (entity.getType()==EntityType.SQUID) {
					Squid ika = (Squid)entity;
					ika.remove();
				}
			}
		}
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
    /**
     * Config として読み込む
     * @param file
     * @return
     */
    public static YamlConfiguration getYml(File file) {
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

	public static String getPluginFolder(){
		return Splatoon.instance.getDataFolder().getAbsolutePath();
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