package info.cerios.electrocraft.computer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.ObjectPair;
import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Utils;

public class ComputerHandler implements IComputerHandler {

	@Override
	public void registerIOPortToAllComputers(NetworkBlock ioPort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerRunnableOnMainThread(IComputerRunnable runnable,
			IComputerCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerComputerTask(XECInterface computer,
			IComputerRunnable runnable, IComputerCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startComputer(TileEntityComputer pc, IComputerCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopComputer(XECInterface pc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetComputer(TileEntityComputer pc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isComputerRunning(XECInterface pc) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void displayComputerGUI(TileEntityComputer pc, EntityPlayer player) {
		GuiPacket guiPacket = new GuiPacket();
		guiPacket.setGui(Gui.COMPUTER_SCREEN);
		if (player instanceof EntityPlayerMP) {
			try {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(guiPacket.getMCPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void displayNetworkGuiScreen(NetworkBlock blockTileEntity, EntityPlayer player) {
		NetworkAddressPacket addressPacket = new NetworkAddressPacket();
		addressPacket.setControlAddress(blockTileEntity.getControlAddress());
		addressPacket.setDataAddress(blockTileEntity.getDataAddress());
		addressPacket.setLocation(blockTileEntity.worldObj.getWorldInfo().getDimension(), blockTileEntity.xCoord, blockTileEntity.yCoord, blockTileEntity.zCoord);
		if (player instanceof EntityPlayerMP) {
			try {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(addressPacket.getMCPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void createAndStartComputer(TileEntityComputer computerBlock,
			IComputerCallback finishedCallback) {
		// TODO Auto-generated method stub
		
	}
}
