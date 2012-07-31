package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;

public interface IComputerHandler {
	public void createAndStartCompuer(TileEntityComputer computerBlock, IComputerCallback finishedCallback);
	public void registerIOPortToAllComputers(NetworkBlock ioPort);
	public void registerRunnableOnMainThread(IComputerRunnable runnable, IComputerCallback callback);
	public void registerComputerTask(IComputer computer, IComputerRunnable runnable, IComputerCallback callback);
	public void update();
	public void startComputer(IComputer pc, IComputerCallback callback);
	public void stopComputer(IComputer pc);
	public void resetComputer(IComputer pc);
	public boolean isComputerRunning(IComputer pc);
	public void displayComputerGUI(IComputer pc);
	public void displayNetworkGuiScreen(NetworkBlock blockTileEntity);
}
