package me.querol.electrocraft.core;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.FutureTask;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class UniversalEventHandler {

    @SubscribeEvent
    public void onTick(TickEvent tick) {
        if (tick.phase == TickEvent.Phase.START && tick.type == TickEvent.Type.SERVER) {
            if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || !FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning())
                return;
            FMLCommonHandler.instance().getMinecraftServerInstance().theProfiler.startSection("ecTickDelegate");
            List<FutureTask<?>> tasks = ElectroCraft.instance.getAndClearTasks();
            for (FutureTask<?> r : tasks) {
                r.run();
            }
            FMLCommonHandler.instance().getMinecraftServerInstance().theProfiler.endSection();
        }
    }
}
