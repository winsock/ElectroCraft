package info.cerios.electrocraft.computer;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Utils;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.ObjectPair;

public class ComputerHandler implements IComputerHandler {
	
	@Override
	public void createAndStartComputer(TileEntityComputer computerBlock,
			IComputerCallback finishedCallback) {
		computerBlock.setComputer(mod_ElectroCraft.instance.getComputer());
		finishedCallback.onTaskComplete(computerBlock);
	}

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
		if (!FMLClientHandler.instance().getClient().isMultiplayerWorld())
			FMLClientHandler.instance().getClient().displayGuiScreen(new GuiComputerScreen(pc.getComputer()));
	}

	@Override
	public void displayNetworkGuiScreen(NetworkBlock blockTileEntity, EntityPlayer player) {
		if (!FMLClientHandler.instance().getClient().isMultiplayerWorld())
			FMLClientHandler.instance().getClient().displayGuiScreen(new GuiNetworkAddressScreen(blockTileEntity));
	}
}
