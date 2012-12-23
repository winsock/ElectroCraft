package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.api.computer.ExposedToLua;
import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.Computer;
import net.minecraft.nbt.NBTTagCompound;

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
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("redstonePower", redstonePower);
        nbttagcompound.setBoolean("receiveMode", receiveMode);
    }

    @Override
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
