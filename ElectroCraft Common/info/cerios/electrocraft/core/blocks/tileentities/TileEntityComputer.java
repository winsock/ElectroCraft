package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Utils;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;

public class TileEntityComputer extends NetworkBlock {

    private Computer computer;
    private Set<NetworkBlock> ioPorts = new HashSet<NetworkBlock>();
    private EntityPlayer activePlayer;

    public TileEntityComputer() {
        this.controlAddress = 4096;
        this.dataAddress = 4097;
        ioPorts.add(this);
    }
    
    public Computer getComputer() {
    	return computer;
    }
    
    public void createComputer() {
    	File bootScript = new File("./electrocraft/boot.rb");
    	if (!bootScript.exists()) {
    		try {
				Utils.copyResource("info/cerios/electrocraft/core/computer/scripts/boot.rb", bootScript);
			} catch (IOException e) {
				FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Error! Could not copy the computer image!");
			}
    	}
		computer = new Computer("./electrocraft/boot.rb", true, 320, 240, 40, 60);
    	
    	if (activePlayer instanceof EntityPlayerMP) {
	        ElectroCraft.instance.getServer().getClient((EntityPlayerMP) activePlayer).setComputer(this);
	        ElectroCraft.instance.getServer().getClient((EntityPlayerMP) activePlayer).sendTerminalSize();
        }
    }
    
    public void startComputer() {
    	if (computer != null) {
//	    	computer.loadIntoMemory(assembledData.data, assembledData.length, assembledData.codeStart);
    		computer.setRunning(true);
	    	new Thread(computer).start();
	    	
//	    	for (NetworkBlock ioPort : ioPorts) {
//	    		computer.registerInterupt(ioPort.getControlAddress(), ioPort);
//	    		computer.registerInterupt(ioPort.getDataAddress(), ioPort);
//	    	}
    	}
    }

    public void setActivePlayer(EntityPlayer player) {
        this.activePlayer = player;
    }

    public EntityPlayer getActivePlayer() {
        return this.activePlayer;
    }

//    public void setComputer(XECCPU xeccpu) {
//        if (this.computer != null)
//            this.computer.stop();
//        this.computer = xeccpu;
//    }
    
    public void registerIoPort(NetworkBlock block) {
//    	if (computer != null && computer.isRunning()) {
//			computer.registerInterupt(block.getControlAddress(), block);
//			computer.registerInterupt(block.getDataAddress(), block);
//		}
//		ioPorts.add(block);
    }

    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }

	@Override
	public Object onTaskComplete(Object... objects) {
//		if (objects[0] instanceof InteruptData) {
//			InteruptData data = (InteruptData)objects[0];
//		}
		return 0;
	}
}
