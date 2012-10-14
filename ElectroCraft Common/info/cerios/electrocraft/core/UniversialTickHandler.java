package info.cerios.electrocraft.core;

import info.cerios.electrocraft.core.computer.IMCRunnable;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.src.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class UniversialTickHandler implements IScheduledTickHandler {
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (((World)tickData[0]).isRemote)
			return;
		FMLCommonHandler.instance().getMinecraftServerInstance().theProfiler.startSection(getLabel());
		List<IMCRunnable> tasks = ElectroCraft.instance.getAndClearTasks();
		for (IMCRunnable r : tasks) {
			r.run(((World)tickData[0]));
		}
		FMLCommonHandler.instance().getMinecraftServerInstance().theProfiler.endSection();
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
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
