package info.cerios.electrocraft.computer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.server.FMLServerHandler;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ModLoader;
import net.minecraft.src.forge.ObjectPair;
import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.AbstractElectroCraftMod;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityComputer;
import info.cerios.electrocraft.core.computer.IComputer;
import info.cerios.electrocraft.core.computer.IComputerCallback;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.IComputerRunnable;
import info.cerios.electrocraft.core.computer.NetworkBlock;
import info.cerios.electrocraft.core.jpc.emulator.PC;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.DefaultVGACard;
import info.cerios.electrocraft.core.jpc.emulator.pci.peripheral.VGACard;
import info.cerios.electrocraft.core.jpc.j2se.VirtualClock;
import info.cerios.electrocraft.core.jpc.support.ArrayBackedSeekableIODevice;
import info.cerios.electrocraft.core.jpc.support.CDROMBlockDevice;
import info.cerios.electrocraft.core.jpc.support.DriveSet;
import info.cerios.electrocraft.core.jpc.support.FileBackedSeekableIODevice;
import info.cerios.electrocraft.core.jpc.support.FloppyBlockDevice;
import info.cerios.electrocraft.core.jpc.support.HDBlockDevice;
import info.cerios.electrocraft.core.jpc.support.TreeBlockDevice;
import info.cerios.electrocraft.core.jpc.support.DriveSet.BootType;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.core.utils.Utils;

public class ComputerHandler implements IComputerHandler {
	private Map<IComputer, ObjectPair<Thread, ComputerThread>> computers = new HashMap<IComputer, ObjectPair<Thread, ComputerThread>>();
    private Map<ObjectTriplet<Integer, Integer, Integer>, IComputer> tileEntitycomputerMap = new HashMap<ObjectTriplet<Integer, Integer, Integer>, IComputer>();
	private Map<ObjectPair<IComputerRunnable, IComputerCallback>, ComputerThread> computerTasks = new HashMap<ObjectPair<IComputerRunnable, IComputerCallback>, ComputerThread>();
	private Map<IComputerRunnable, IComputerCallback> waitingTasks = new HashMap<IComputerRunnable, IComputerCallback>();

	public void createAndStartComputer(TileEntityComputer computerBlock, IComputerCallback finishedCallback) {
		registerRunnableOnMainThread(new IComputerRunnable() {

			TileEntityComputer computerBlock;
			
			public IComputerRunnable init(TileEntityComputer computerBlock) {
				this.computerBlock = computerBlock;
				return this;
			}
			
			@Override
			public Object run() {
				IComputer pc = null;
				try {
					FloppyBlockDevice bootDrive = new FloppyBlockDevice(new ArrayBackedSeekableIODevice("info/cerios/electrocraft/core/jpc/resources/images/slitaz.img"));
					
					File electroCraftFolder = new File(FMLCommonHandler.instance().getMinecraftRootDirectory() + File.separator + "electrocraft");
					if (!electroCraftFolder.exists())
						electroCraftFolder.mkdirs();
					
					File sharedPCFolder = new File(electroCraftFolder.getAbsolutePath() + File.separator + "sharedfolder");
					if (!sharedPCFolder.exists())
						sharedPCFolder.mkdirs();
					
					File computerHddFile = new File(electroCraftFolder.getAbsolutePath() + File.separator + "computer" + String.valueOf(computerBlock.xCoord) + String.valueOf(computerBlock.yCoord) + String.valueOf(computerBlock.zCoord) + ".img");
					if (!computerHddFile.exists())
						Utils.copyResource("info/cerios/electrocraft/core/jpc/resources/images/blankhdd", computerHddFile);
					
					HDBlockDevice computerHdd = new HDBlockDevice(new FileBackedSeekableIODevice(computerHddFile.getAbsolutePath()));
					TreeBlockDevice hostFolder = new TreeBlockDevice(sharedPCFolder, false);
					
					DriveSet drives = new DriveSet(BootType.FLOPPY, bootDrive, null, computerHdd, hostFolder, null, null);
					
					VirtualClock clock = new VirtualClock();
					clock.setIPS(10000000); // Slow down the server computers
					pc = (IComputer) new PC(clock, drives);
					computerBlock.setComputer(pc);
					pc.reset();
					// Set the default resolution
					((DefaultVGACard)pc.getComponent(VGACard.class)).resizeDisplay(720, 480);
					ComputerThread computerThreadObject = new ComputerThread(computerBlock);
					Thread computerThread = new Thread(computerThreadObject);
					pc.start();
					computerThread.start();
					computers.put(pc, new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
                    tileEntitycomputerMap.put(new ObjectTriplet<Integer, Integer, Integer>(computerBlock.xCoord, computerBlock.yCoord, computerBlock.zCoord), pc);
				} catch (IOException e) {
					ModLoader.getLogger().severe("ElectroCraft: Unable to start JPC Emulator!");
				}
				return computerBlock;
			}}.init(computerBlock), finishedCallback);
	}
    
    public IComputer getComputer(TileEntityComputer computer) {
        ObjectTriplet<Integer, Integer, Integer> location = new ObjectTriplet<Integer, Integer, Integer>(computer.xCoord, computer.yCoord, computer.zCoord);
        return tileEntitycomputerMap.get(location);
    }

	public void registerIOPortToAllComputers(NetworkBlock ioPort) {
		for (IComputer computer : computers.keySet()) {
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
	
	public void registerComputerTask(IComputer computer, IComputerRunnable runnable, IComputerCallback callback) {
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

	public void startComputer(TileEntityComputer pc, IComputerCallback callback) {
		registerRunnableOnMainThread(new IComputerRunnable() {

			private TileEntityComputer pc;

			public IComputerRunnable init(TileEntityComputer pc) {
				this.pc = pc;
				return this;
			}

			@Override
			public Object run() {
				// Set the default resolution
				((DefaultVGACard)pc.getComputer().getComponent(VGACard.class)).resizeDisplay(720, 480);
				if (computers.containsKey(pc)) {
					computers.remove(pc);
				}
				pc.getComputer().start();
				ComputerThread computerThreadObject = new ComputerThread(pc);
				Thread computerThread = new Thread(computerThreadObject);
				computerThread.start();
				pc.getComputer().reset();
				computers.put(pc.getComputer(), new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
				return pc;
			}
		}.init(pc), callback);
	}

	public boolean isComputerRunning(IComputer pc) {
		if (computers.containsKey(pc)) {
			return computers.get(pc).getValue2().getRunning();
		}
		return false;
	}

	public void stopComputer(IComputer pc) {
		registerComputerTask(pc, new IComputerRunnable() {

			private IComputer pc;

			public IComputerRunnable init(IComputer pc) {
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

	public void resetComputer(TileEntityComputer pc) {
		registerComputerTask(pc.getComputer(), new IComputerRunnable() {

			private TileEntityComputer pc;

			public IComputerRunnable init(TileEntityComputer pc) {
				this.pc = pc;
				return this;
			}

			@Override
			public Object run() {
				pc.getComputer().stop();
				pc.getComputer().reset();
				// Set the default resolution
				((DefaultVGACard)pc.getComputer().getComponent(VGACard.class)).resizeDisplay(720, 480);
				pc.getComputer().start();
				ComputerThread computerThreadObject = new ComputerThread(pc);
				Thread computerThread = new Thread(computerThreadObject);
				computerThread.start();
				pc.getComputer().reset();
				computers.put(pc.getComputer(), new ObjectPair<Thread, ComputerThread>(computerThread, computerThreadObject));
				return pc;
			}
		}.init(pc), null);
	}

	public void stopAllComputers() {
		for (IComputer pc : computers.keySet())
			stopComputer(pc);
	}

	private class ComputerThread implements Runnable {

		private TileEntityComputer pc;
		private volatile boolean running = true;

		public ComputerThread(TileEntityComputer pc) {
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
					pc.getComputer().execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
				pc.getComputer().reset();
				ModLoader.getLogger().log(Level.INFO, "ElectroCraft: PC Threw an exception while executing!");
				// Close the GUI if it is still open
				AbstractElectroCraftMod.getInstance().getSidedMethods().closeGui(pc.getActivePlayer());
			}
			pc.getComputer().stop();
			ModLoader.getLogger().log(Level.INFO, "ElectroCraft: PC Stopped");
			running = false;
		}
	}

	@Override
	public void displayComputerGUI(TileEntityComputer pc, EntityPlayer player) {
		GuiPacket guiPacket = new GuiPacket();
		guiPacket.setGui(Gui.COMPUTER_SCREEN);
		if (player instanceof EntityPlayerMP) {
			try {
				mod_ElectroCraft.instance.getServer().getClient((EntityPlayerMP) player).setComputer(pc);
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(guiPacket.getMCPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void displayNetworkGuiScreen(NetworkBlock blockTileEntity, EntityPlayer player) {
		NetworkAddressPacket addressPacket = new NetworkAddressPacket();
		addressPacket.setControlAddress(blockTileEntity.getControlAddress());
		addressPacket.setDataAddress(blockTileEntity.getDataAddress());
		addressPacket.setLocation(blockTileEntity.worldObj.getWorldInfo().getDimension(), blockTileEntity.xCoord, blockTileEntity.yCoord, blockTileEntity.zCoord);
		if (player instanceof EntityPlayerMP) {
			try {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(addressPacket.getMCPacket());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
