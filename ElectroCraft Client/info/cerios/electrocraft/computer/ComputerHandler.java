package info.cerios.electrocraft.computer;

import cpw.mods.fml.client.FMLClientHandler;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.*;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;
import info.cerios.electrocraft.mod_ElectroCraft;
import net.minecraft.src.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public class ComputerHandler implements IComputerHandler {

    private Map<IComputerRunnable, IComputerCallback> callbacks = new HashMap<IComputerRunnable, IComputerCallback>();

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
        callbacks.put(runnable, callback);
    }

    @Override
    public void registerComputerTask(XECInterface computer,
                                     IComputerRunnable runnable, IComputerCallback callback) {
        // TODO Auto-generated method stub

    }

    @Override
    public void update() {
        Map<IComputerRunnable, IComputerCallback> tempCallbacks = new HashMap<IComputerRunnable, IComputerCallback>(callbacks);
        for (IComputerRunnable runnable : tempCallbacks.keySet()) {
            callbacks.get(runnable).onTaskComplete(runnable.run());
        }
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
