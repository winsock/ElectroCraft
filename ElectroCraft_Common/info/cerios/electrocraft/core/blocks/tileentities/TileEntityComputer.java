package info.cerios.electrocraft.core.blocks.tileentities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;

import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.utils.ObjectTriplet;

public class TileEntityComputer extends NetworkBlock {

	private XECInterface computer;
	private Set<ObjectTriplet<Integer, Integer, Integer>> ioPorts = new HashSet<ObjectTriplet<Integer, Integer, Integer>>();
	private EntityPlayer activePlayer;
	
	public TileEntityComputer() {
		this.controlAddress = 223;
		this.dataAddress = 224;
		ioPorts.add(new ObjectTriplet<Integer, Integer, Integer>(xCoord, yCoord, zCoord));
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		NBTTagList ioPortList = new NBTTagList("ioPorts");
		for (ObjectTriplet<Integer, Integer, Integer> ioPort: ioPorts) {
			NBTTagCompound ioPortData = new NBTTagCompound("ioPortData");
			ioPortData.setInteger("x", ioPort.getValue1());
			ioPortData.setInteger("y", ioPort.getValue2());
			ioPortData.setInteger("z", ioPort.getValue3());
			ioPortList.appendTag(ioPortData);
		}
		nbttagcompound.setTag("ioPorts", ioPortList);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		NBTTagList ioPortList = nbttagcompound.getTagList("ioPorts");
		for (int i = 0; i < ioPortList.tagCount(); i++) {
			if (ioPortList.tagAt(i) instanceof NBTTagCompound) {
				NBTTagCompound ioPortData = (NBTTagCompound) ioPortList.tagAt(i);
				ioPorts.add(new ObjectTriplet<Integer, Integer, Integer>(ioPortData.getInteger("x"), ioPortData.getInteger("y"), ioPortData.getInteger("z")));
			}
		}
	}
	
	public XECInterface getComputer() {
		return computer;
	}
	
	public void setActivePlayer(EntityPlayer player) {
		this.activePlayer = player;
	}
	
	public EntityPlayer getActivePlayer() {
		return this.activePlayer;
	}
	
	public void setComputer(XECInterface computer) {
		if (this.computer != null)
			AbstractElectroCraftMod.getInstance().getComputerHandler().stopComputer(this.computer);
		this.computer = computer;
	}
	
	@Override
	public boolean canConnectNetwork(NetworkBlock block) {
		return true;
	}
}
