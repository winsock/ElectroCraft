package me.querol.electrocraft.api;

import me.querol.electrocraft.api.drone.tools.IDroneTool;
import me.querol.electrocraft.api.drone.upgrade.ICard;
import me.querol.electrocraft.api.drone.upgrade.ICard;

import java.util.concurrent.FutureTask;

public interface IElectroCraft {
    public void registerAddon(Object mod);

    public void regsiterCard(ICard card);

    public void registerDroneTool(IDroneTool tool);
}
