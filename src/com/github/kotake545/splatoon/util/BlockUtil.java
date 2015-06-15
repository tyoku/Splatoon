package com.github.kotake545.splatoon.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.github.kotake545.splatoon.Splatoon;
import com.github.kotake545.splatoon.Packet.ParticleAPI;
import com.github.kotake545.splatoon.Packet.ParticleAPI.EnumParticle;

public class BlockUtil {
	public Hashtable<Block,Integer[]> Blocks;

	public BlockUtil() {
		Blocks = new Hashtable<Block,Integer[]>();
	}

	/**
	 * int X Y Z world でチェックするようにしてる過程でどんどん汚くなったのであとで整理
	 */

	@SuppressWarnings("deprecation")
	public void setBlock(Location loc,Integer[] block){
		if(block!=null){
			if(containsCanselBlocks(getIntegerBlock(loc))){
				return;
			}
			if(Splatoon.ikaConfig.blockset){
				setBeforeBlock(loc.getBlock());
				loc.getBlock().setTypeIdAndData(block[0],(byte)(int)block[1],true);
			}else{
				setPacketBlock(loc.getBlock(), block);
			}
		}
	}

	public static boolean containsCanselBlocks(Integer[] block){
		for(Integer[] blocks:Splatoon.ikaConfig.inkCanselBlocks){
			if(block[0]==blocks[0]&&block[1]==blocks[1]){
				return true;
			}
		}
		return false;
	}

	public static List<Vector> getCircleVectors(Location lastloc,int radius){
		List<Vector> vectors = new ArrayList<Vector>();
        Location location = lastloc.clone();
        for(int r = 0; r < radius; r++){
	        double x, y = location.getY(), z;
	        location.add(0, y, 0);
		    int particles = r^2;
	        for (int i = 0; i < particles; i++) {
	            double angle = (double) 2 * Math.PI * i / particles;
	            x = Math.cos(angle) * r;
	            z = Math.sin(angle) * r;
	            location.add(x, 0, z);
	            try {
	            	ParticleAPI.sendAllPlayer(EnumParticle.VILLAGER_HAPPY, location, 0,0, 0,(float)0.3,1);
				} catch (Exception e) {
					e.printStackTrace();
				}
	            vectors.add(lastloc.subtract(location).toVector());

	            location.subtract(x, 0, z);
	        }
	        location.subtract(0, y, 0);
        }
        return vectors;
	}

	public static List<Block> getCircleBlocks(Location loc,int radius){
		List<Block> blocks = new ArrayList<Block>();
		Block block = loc.getBlock();
		Location location = new Location(block.getWorld(),block.getX()-0.5,block.getY(),block.getZ()+0.5);
		Location first = location.clone();
        int x = 0;
        int y = location.getBlockY();
        int z = 0;
        for(int r = 0; r < radius; r++){
        	/**
        	 * 円を一周描く
        	 */
        	//r=1:check=1 , r=2:check=4, r=3:check=14 r=4:check=16 r=5:check=32 r=6:check=16
        	double check = 0;
        	if(0==r)check=1;
        	if(1==r)check=4;
        	//ココの下から法則性??????
        	if(2==r)check=16;
        	if(3==r)check=70;
        	if(4<=r&&r<=5)check=32;
        	if(6==r)check=64;
        	if(7==r)check=64;
        	if(8<=r)check=360;
        	for (double i = 0.0; i < check; i ++) {
            	double angle = (double) 2 * Math.PI * i / check;
                x = (int)(location.getX() + r * Math.cos(angle));
                z = (int)(location.getZ() + r * Math.sin(angle));

                Block bloc2 = location.getWorld().getBlockAt(x,y,z);
                if(!blocks.contains(bloc2)){
                	blocks.add(bloc2);
                }
            }
        }
        return blocks;
    }

	public void setPacketBlock(Block block,Integer[] set){
		for(Block b:Blocks.keySet()){
			if(b.getX()==block.getX()&&b.getY()==block.getY()&&b.getZ()==block.getZ()&&b.getWorld()==block.getWorld()){
				Blocks.put(b,set);
			}
		}
		Splatoon.blockUtil.Blocks.put(block,set);
	}

	public boolean setBeforeBlock(Block block){
		if(!containsBlock(block)){
			Integer[] b = new Integer[]{0,0};
			b[0] = block.getTypeId();
			b[1] = (int)block.getData();
			Blocks.put(block,b);
			return true;
		}
		return false;
	}
	public boolean containsBlock(Block block){
		for(Block b:Blocks.keySet()){
			if(b.getX()==block.getX()&&b.getY()==block.getY()&&b.getZ()==block.getZ()&&b.getWorld()==block.getWorld()){
				return true;
			}
		}
		return false;
	}

	public Integer[] getIntegerBlock(Location loc) {
		Integer[] block = new Integer[]{0,0};
		block[0] =loc.getBlock().getTypeId();
		block[1] =(int) loc.getBlock().getData();
		return block;
	}

	public Integer[] getBlock(Block block) {
		for(Block b:Blocks.keySet()){
			if(b.getX()==block.getX()&&b.getY()==block.getY()&&b.getZ()==block.getZ()&&b.getWorld()==block.getWorld()){
				return Blocks.get(b);
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public boolean returnBeforeBlocks(){
		if(Blocks.size()>0){
			if(Splatoon.ikaConfig.blockset){
				int i = 0;
				for(Block block:Blocks.keySet()){
					Integer[] b = Blocks.get(block);
					block.setTypeIdAndData(b[0],(byte)(int)b[1],true);
					i++;
				}
				Blocks = new Hashtable<Block, Integer[]>();
				Splatoon.logger.info(i+"つ のブロックの修復に成功しました。");
			}else{
				for(Player player:Splatoon.getOnlinePlayers()){
					for(Block block:Blocks.keySet()){
						Block a = block.getLocation().getBlock();
						player.sendBlockChange(block.getLocation(),a.getTypeId(),a.getData());
					}
				}
				Blocks = new Hashtable<Block, Integer[]>();
			}
			return true;
		}
		return false;
	}
	/**
	 * フェンス抜け
	 * 抜けるプレイヤー以外にはフェンスが存在するパケットを送り続ける。
	 * 抜けるプレイヤーにはフェンスが映らない Worldの方も0 にしないとチート検知に引っかかるので
	 * Blocksに登録しておくこと
	 */
//	public static boolean isFence(Block block){
//		if(Splatoon.ikaConfig.fenceBlocks.size()<=0){
//			return false;
//		}
//		Integer[] a = new Integer[]{0,0};
//		a[0]=block.getTypeId();
//		a[1]=(int) block.getData();
//		if(Splatoon.ikaConfig.fenceBlocks.contains(a)){
//			return true;
//		}else{
//			return false;
//		}
//	}
}
