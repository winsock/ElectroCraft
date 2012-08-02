package info.cerios.electrocraft.core.computer;

import net.minecraft.src.EntityPlayer;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;

public interface IComputerHandler {
	public void createAndStartCompuer(TileEntityComputer computerBlock, IComputerCallback finishedCallback);
	public void registerIOPortToAllComputers(NetworkBlock ioPort);
	public void registerRunnableOnMainThread(IComputerRunnable runnable, IComputerCallback callback);
	public void registerComputerTask(IComputer computer, IComputerRunnable runnable, IComputerCallback callback);
	public void update();
	public void startComputer(TileEntityComputer pc, IComputerCallback callback);
	public void stopComputer(IComputer pc);
	public void resetComputer(TileEntityComputer pc);
	public boolean isComputerRunning(IComputer pc);
	public void displayComputerGUI(TileEntityComputer pc, EntityPlayer player);
	public void displayNetworkGuiScreen(NetworkBlock blockTileEntity, EntityPlayer player);
}
