package info.cerios.electrocraft.api;

import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.api.drone.upgrade.ICard;

import java.util.concurrent.FutureTask;

public interface IElectroCraft {
    public void registerAddon(Object mod);

    public void regsiterCard(ICard card);

    public void registerDroneTool(IDroneTool tool);

    public void registerRunnable(FutureTask<?> task);
}
