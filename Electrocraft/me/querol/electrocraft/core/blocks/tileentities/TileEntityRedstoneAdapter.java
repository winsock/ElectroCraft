package me.querol.electrocraft.core.blocks.tileentities;

import me.querol.electrocraft.api.computer.ExposedToLua;
import me.querol.electrocraft.api.computer.NetworkBlock;
import me.querol.electrocraft.core.blocks.ElectroBlocks;
import me.querol.electrocraft.core.computer.Computer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;

@ExposedToLua
public class TileEntityRedstoneAdapter extends NetworkBlock implements IUpdatePlayerListBox {

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
    public void update() {
        super.update();
        if (outputChanged) {
            worldObj.markBlockForUpdate(pos);
            worldObj.notifyNeighborsOfStateChange(pos, ElectroBlocks.REDSTONE_ADAPTER.getBlock());
            outputChanged = false;
        }
        if (externalPower != (worldObj.isBlockIndirectlyGettingPowered(pos) != 0)) {
            setExternalState(worldObj.isBlockIndirectlyGettingPowered(pos) != 0);
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
