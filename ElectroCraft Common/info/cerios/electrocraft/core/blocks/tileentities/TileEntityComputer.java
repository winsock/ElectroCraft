package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.XECCPU;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.computer.XECCPU.InteruptData;
import info.cerios.electrocraft.core.computer.XECInterface.AssembledData;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Utils;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TileEntityComputer extends NetworkBlock {

    private XECCPU computer;
    private Set<NetworkBlock> ioPorts = new HashSet<NetworkBlock>();
    private EntityPlayer activePlayer;
    private AssembledData assembledData;

    public TileEntityComputer() {
        this.controlAddress = 4096;
        this.dataAddress = 4097;
        ioPorts.add(this);
    }

    public void createXECCPU() {
    	computer = ElectroCraft.instance.getComputerInterface().createCPU(320, 240, 40, 60, 8 * 1024 * 1024, 4096, 5000000);
    	
        String startupAsm = Utils.loadUncompiledAssembly("." + File.separator + "electrocraft" + File.separator + "startup.xsm");
        if (startupAsm.isEmpty()) {
        	assembledData = computer.assemble(".code\nhlt");
        } else {
        	assembledData = computer.assemble(startupAsm);
        }
        
        if (activePlayer instanceof EntityPlayerMP) {
	        ElectroCraft.instance.getServer().getClient((EntityPlayerMP) activePlayer).setComputer(this);
	        ElectroCraft.instance.getServer().getClient((EntityPlayerMP) activePlayer).sendTerminalSize();
        }
    }
    
    public XECCPU getComputer() {
    	return computer;
    }
    
    public void startComputer() {
    	if (computer != null) {
	    	long baseAddress = computer.loadIntoMemory(assembledData.data, assembledData.length, assembledData.codeStart);
	    	computer.start(baseAddress);
	    	
	    	for (NetworkBlock ioPort : ioPorts) {
	    		computer.registerInterupt(ioPort.getControlAddress(), ioPort);
	    		computer.registerInterupt(ioPort.getDataAddress(), ioPort);
	    	}
    	}
    }

    public void setActivePlayer(EntityPlayer player) {
        this.activePlayer = player;
    }

    public EntityPlayer getActivePlayer() {
        return this.activePlayer;
    }

    public void setComputer(XECCPU xeccpu) {
        if (this.computer != null)
            this.computer.stop();
        this.computer = xeccpu;
    }
    
    public void registerIoPort(NetworkBlock block) {
    	if (computer != null && computer.isRunning()) {
			computer.registerInterupt(block.getControlAddress(), block);
			computer.registerInterupt(block.getDataAddress(), block);
		}
		ioPorts.add(block);
    }

    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }

	@Override
	public Object onTaskComplete(Object... objects) {
		if (objects[0] instanceof InteruptData) {
			InteruptData data = (InteruptData)objects[0];
		}
		return 0;
	}
}
