package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.jpc.emulator.PC;
import info.cerios.electrocraft.core.jpc.j2se.VirtualClock;
import info.cerios.electrocraft.core.jpc.support.Clock;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.gui.GuiComputerScreen;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.ObjectPair;

public class ComputerHandler {
	private Map<PC, ObjectPair<Thread, ComputerThread>> computers = new HashMap<PC, ObjectPair<Thread, ComputerThread>>();
	
	public PC createAndStartCompuer(TileEntityComputer computerBlock) {
		String[] args = new String[]{
				"-hda", "mem:info/cerios/electrocraft/core/jpc/resources/images/ElectroCraftBase.img",
				"-boot", "hda"
            };
		PC pc = null;
        try {
			pc = new PC(new VirtualClock(), args);
			pc.addPart(computerBlock);
			ComputerThread computerThreadObject = new ComputerThread(pc);
			Thread computerThread = new Thread(computerThreadObject);
			computerThread.start();
			computers.put(pc, new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
		} catch (IOException e) {
			ModLoader.getLogger().severe("ElectroCraft: Unable to start JPC Emulator!");
		}
        
        return pc;
	}
	
	public void registerIOPortToAllComputers(IOPortCapableMinecraft ioPort) {
		for (PC computer : computers.keySet()) {
			stopComputer(computer);
			computer.addPart(ioPort);
		}
	}
	
	public void startComputer(PC pc) {
		if (computers.containsKey(pc)) {
			computers.get(pc).getValue2().setRunning(true);
			computers.get(pc).setValue1(new Thread(computers.get(pc).getValue2()));
			computers.get(pc).getValue1().start();
		} else {
			ComputerThread computerThreadObject = new ComputerThread(pc);
			Thread computerThread = new Thread(computerThreadObject);
			computerThread.start();
			computers.put(pc, new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
		}
	}
	
	public boolean isComputerRunning(PC pc) {
		if (computers.containsKey(pc)) {
			return computers.get(pc).getValue2().getRunning();
		}
		return false;
	}
	
	public void stopComputer(PC pc) {
		computers.get(pc).getValue2().setRunning(false);
		computers.get(pc).getValue1().interrupt();
		pc.stop();
	}
	
	public void stopAllComputers() {
		for (PC pc : computers.keySet())
			stopComputer(pc);
	}
	
	private class ComputerThread implements Runnable {

		private PC pc;
		private volatile boolean running = true;
		
		public ComputerThread(PC pc) {
			this.pc = pc;
		}
		
		public synchronized void setRunning(boolean running) {
			this.running = running;
		}
		
		public synchronized boolean getRunning() {
			return running;
		}
		
		@Override
		public void run() {
			pc.start();
			try {
				while (running) {
					pc.execute();
				}
			} catch (Exception e) {
	            ModLoader.getLogger().log(Level.INFO, "ElectroCraft: PC Threw an exception while executing!");
			}
	        pc.stop();
            ModLoader.getLogger().log(Level.INFO, "ElectroCraft: PC Stopped");
            running = false;
		}
	}
}
