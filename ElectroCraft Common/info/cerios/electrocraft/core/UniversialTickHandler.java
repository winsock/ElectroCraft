package info.cerios.electrocraft.core;

import info.cerios.electrocraft.api.computer.IComputerCallback;
import info.cerios.electrocraft.api.computer.IComputerRunnable;
import info.cerios.electrocraft.api.computer.IMCRunnable;
import info.cerios.electrocraft.api.utils.ObjectPair;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.FutureTask;

import net.minecraft.src.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class UniversialTickHandler implements IScheduledTickHandler {
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || !FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning())
			return;
		FMLCommonHandler.instance().getMinecraftServerInstance().theProfiler.startSection(getLabel());
		List<FutureTask<?>> tasks = ElectroCraft.instance.getAndClearTasks();
		for (FutureTask<?> r : tasks) {
			r.run();
		}
		FMLCommonHandler.instance().getMinecraftServerInstance().theProfiler.endSection();
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return "ecTickDelegate";
	}

	@Override
	public int nextTickSpacing() {
		return 1;
	}
}
