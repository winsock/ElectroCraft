package info.cerios.electrocraft.core.blocks.tileentities;

import java.util.EnumSet;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ExposedToLua;
import info.cerios.electrocraft.core.computer.IMCRunnable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

@ExposedToLua
public class TileEntityRedstoneAdapter extends NetworkBlock {

    private boolean redstonePower = false;
    private boolean externalPower = false;
    private boolean receiveMode = false;
    private boolean outputChanged = false;
    private boolean inputChanged = false;

    public TileEntityRedstoneAdapter() {
        this.dataAddress = 4098;
        this.controlAddress = 4099;
    }

    @Override
    public void updateEntity() {
    	super.updateEntity();
    	if (outputChanged) {
    		worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
    		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, ElectroBlocks.REDSTONE_ADAPTER.getBlock().blockID);
    		outputChanged = false;
    	}
    	if (externalPower != worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
    		setExternalState(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord));
    	}
    }
    
    public boolean getRedstonePower() {
        return redstonePower;
    }

    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("redstonePower", redstonePower);
        nbttagcompound.setBoolean("receiveMode", receiveMode);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.redstonePower = nbttagcompound.getBoolean("redstonePower");
        this.receiveMode = nbttagcompound.getBoolean("receiveMode");
    }

    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }

    @ExposedToLua
    public boolean isInReceiveMode() {
    	return receiveMode;
    }
    
    @ExposedToLua
    public void setReceiveMode(boolean value) {
    	receiveMode = value;
    }
    
    @ExposedToLua
    public boolean getState() {
    	return receiveMode ? externalPower : redstonePower;
    }
    
    @ExposedToLua
    public void setState(final boolean state) {
    	if (redstonePower != state) {
    		outputChanged = true;
    	}
    	redstonePower = state;
    }
    
    @ExposedToLua(value = false)
    public void setExternalState(boolean state) {
    	if (receiveMode && state != externalPower) {
    		inputChanged = true;
    	}
    	externalPower = state;
    }
    
    @ExposedToLua(value = false)
	@Override
	public void tick(Computer computer) {
		if (inputChanged) {
			computer.postEvent("rs", dataAddress, externalPower);
			inputChanged = false;
		}
	}
}
