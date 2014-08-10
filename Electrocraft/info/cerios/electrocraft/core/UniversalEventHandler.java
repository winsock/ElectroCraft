package info.cerios.electrocraft.core;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.FutureTask;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

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
