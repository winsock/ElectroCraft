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
import net.minecraftforge.common.ForgeDirection;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;

public class TileEntityComputer extends NetworkBlock implements IDirectionalBlock {

    private Computer computer;
    private Set<NetworkBlock> ioPorts = new HashSet<NetworkBlock>();
    private EntityPlayer activePlayer;
    private ForgeDirection direction = ForgeDirection.NORTH;
    
    /**
     * The default base directory for a new computer, its format is as follows
     * XYZEpochTime
     * Where X = the x position of the block, Y = the y position of the block, Z = the z position of the block
     * EpochTime = Milliseconds since the Unix Epoch
     */
    private String baseDirectory = "";

    public TileEntityComputer() {
        this.controlAddress = 4096;
        this.dataAddress = 4097;
        ioPorts.add(this);
    }
    
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setString("baseDirectory", baseDirectory);
        nbttagcompound.setInteger("direction", direction.ordinal());
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.baseDirectory = nbttagcompound.getString("baseDirectory");
        this.direction = ForgeDirection.values()[nbttagcompound.getInteger("direction")];
    }
    
    public Computer getComputer() {
    	return computer;
    }
    
    public void createComputer() {
    	if (activePlayer instanceof EntityPlayerMP) {
    		if (this.baseDirectory.isEmpty())
    	        this.baseDirectory = "./electrocraft/computers/" + String.valueOf(Math.abs(this.xCoord)) + String.valueOf(Math.abs(this.yCoord)) + String.valueOf(Math.abs(this.zCoord)) + String.valueOf(Calendar.getInstance().getTime().getTime());

    		computer = new Computer(ElectroCraft.instance.getServer().getClient((EntityPlayerMP) activePlayer), "", baseDirectory, true, 320, 240, 40, 60);
        } else {
    		computer = new Computer(null, "", baseDirectory, true, 320, 240, 40, 60);
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
	
	@Override
	public void setDirection(ForgeDirection direction) {
		this.direction = direction;
	}

	@Override
	public ForgeDirection getDirection() {
		return this.direction;
	}
}
