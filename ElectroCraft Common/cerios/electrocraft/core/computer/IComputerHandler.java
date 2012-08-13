package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import net.minecraft.src.EntityPlayer;

public interface IComputerHandler {
    public void createAndStartComputer(TileEntityComputer computerBlock, IComputerCallback finishedCallback);

    public void registerIOPortToAllComputers(NetworkBlock ioPort);

    public void registerRunnableOnMainThread(IComputerRunnable runnable, IComputerCallback callback);

    public void registerComputerTask(XECInterface computer, IComputerRunnable runnable, IComputerCallback callback);

    public void update();

    public void startComputer(TileEntityComputer pc, IComputerCallback callback);

    public void stopComputer(XECInterface pc);

    public void resetComputer(TileEntityComputer pc);

    public boolean isComputerRunning(XECInterface pc);

    public void displayComputerGUI(TileEntityComputer pc, EntityPlayer player);

    public void displayNetworkGuiScreen(NetworkBlock blockTileEntity, EntityPlayer player);
}
