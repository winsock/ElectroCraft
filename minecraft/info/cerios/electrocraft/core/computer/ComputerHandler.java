package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.jpc.emulator.PC;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.DefaultVGACard;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.VGACard;
import info.cerios.electrocraft.core.jpc.j2se.VirtualClock;
import info.cerios.electrocraft.core.jpc.support.Clock;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Utils;
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
	private Map<ObjectPair<IComputerRunnable, IComputerCallback>, ComputerThread> computerTasks = new HashMap<ObjectPair<IComputerRunnable, IComputerCallback>, ComputerThread>();
	private Map<IComputerRunnable, IComputerCallback> waitingTasks = new HashMap<IComputerRunnable, IComputerCallback>();

	public void createAndStartCompuer(TileEntityComputer computerBlock, IComputerCallback finishedCallback) {
		registerRunnableOnMainThread(new IComputerRunnable() {

			TileEntityComputer computerBlock;
			
			public IComputerRunnable init(TileEntityComputer computerBlock) {
				this.computerBlock = computerBlock;
				return this;
			}
			
			@Override
			public Object run() {
				String[] args = new String[]{
						"-hda", "mem:info/cerios/electrocraft/core/jpc/resources/images/ElectroCraftBase.img",
						"-boot", "hda"
				};
				PC pc = null;
				try {
					pc = new PC(new VirtualClock(), args);
					computerBlock.setComputer(pc);
					pc.reset();
					// Set the default resolution
					((DefaultVGACard)pc.getComponent(VGACard.class)).resizeDisplay(720, 480);
					ComputerThread computerThreadObject = new ComputerThread(pc);
					Thread computerThread = new Thread(computerThreadObject);
					pc.start();
					computerThread.start();
					computers.put(pc, new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
				} catch (IOException e) {
					ModLoader.getLogger().severe("ElectroCraft: Unable to start JPC Emulator!");
				}
				return pc;
			}}.init(computerBlock), finishedCallback);
	}

	public void registerIOPortToAllComputers(IOPortCapableMinecraft ioPort) {
		for (PC computer : computers.keySet()) {
			computer.addPart(ioPort);
		}
	}
	
	/**
	 * Add a task to the queue, order shouldn't matter
	 */
	public void registerRunnableOnMainThread(IComputerRunnable runnable, IComputerCallback callback) {
		synchronized(waitingTasks) {
			this.waitingTasks.put(runnable, callback);
		}
	}
	
	public void registerComputerTask(PC computer, IComputerRunnable runnable, IComputerCallback callback) {
		synchronized(computerTasks) {
			if (computers.containsKey(computer))
				computerTasks.put(new ObjectPair<IComputerRunnable, IComputerCallback>(runnable, callback), computers.get(computer).getValue2());
		}
	}

	public void update() {
		synchronized (waitingTasks) {
			for (IComputerRunnable task : waitingTasks.keySet()) {
				if (waitingTasks.get(task) != null)
					waitingTasks.get(task).onTaskComplete(task.run());
				else
					task.run();
			}
			waitingTasks.clear();
		}
	}

	public void startComputer(PC pc, IComputerCallback callback) {
		registerRunnableOnMainThread(new IComputerRunnable() {

			private PC pc;

			public IComputerRunnable init(PC pc) {
				this.pc = pc;
				return this;
			}

			@Override
			public Object run() {
				// Set the default resolution
				((DefaultVGACard)pc.getComponent(VGACard.class)).resizeDisplay(720, 480);
				if (computers.containsKey(pc)) {
					computers.remove(pc);
				}
				pc.start();
				ComputerThread computerThreadObject = new ComputerThread(pc);
				Thread computerThread = new Thread(computerThreadObject);
				computerThread.start();
				computers.put(pc, new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
				return pc;
			}
		}.init(pc), callback);
	}

	public boolean isComputerRunning(PC pc) {
		if (computers.containsKey(pc)) {
			return computers.get(pc).getValue2().getRunning();
		}
		return false;
	}

	public void stopComputer(PC pc) {
		registerComputerTask(pc, new IComputerRunnable() {

			private PC pc;

			public IComputerRunnable init(PC pc) {
				this.pc = pc;
				return this;
			}

			@Override
			public Object run() {
				pc.stop();
				computers.get(pc).getValue2().setRunning(false);
				computers.get(pc).getValue1().interrupt();
				return pc;
			}
		}.init(pc), null);
	}

	public void resetComputer(PC pc) {
		registerComputerTask(pc, new IComputerRunnable() {

			private PC pc;

			public IComputerRunnable init(PC pc) {
				this.pc = pc;
				return this;
			}

			@Override
			public Object run() {
				pc.stop();
				pc.reset();
				// Set the default resolution
				((DefaultVGACard)pc.getComponent(VGACard.class)).resizeDisplay(720, 480);
				pc.start();
				if (computers.containsKey(pc)) {
					if (!isComputerRunning(pc)) {
						computers.get(pc).getValue2().setRunning(true);
						computers.get(pc).setValue1(new Thread(computers.get(pc).getValue2()));
						computers.get(pc).getValue1().start();
					}
				} else {
					ComputerThread computerThread = new ComputerThread(pc);
					Thread thread = new Thread(computerThread);
					thread.start();
					computers.put(pc, new ObjectPair<Thread, ComputerThread>(thread, computerThread));
				}
				return pc;
			}
		}.init(pc), null);
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
			try {
				while (running && !Thread.interrupted()) {
					synchronized(computerTasks){
						if (computerTasks.containsValue(this)) {
							for (ObjectPair<IComputerRunnable, IComputerCallback> task : Utils.getKeysByValue(computerTasks, this)) {
								if (task.getValue2() != null)
									task.getValue2().onTaskComplete(task.getValue1().run());
								else
									task.getValue1().run();
								computerTasks.remove(task);
							}
						}
					}
					
					pc.execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
				pc.reset();
				ModLoader.getLogger().log(Level.INFO, "ElectroCraft: PC Threw an exception while executing!");
				// Close the GUI if it is still open
				if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiComputerScreen)
					ModLoader.getMinecraftInstance().displayGuiScreen(null);
			}
			pc.stop();
			ModLoader.getLogger().log(Level.INFO, "ElectroCraft: PC Stopped");
			running = false;
		}
	}
}
