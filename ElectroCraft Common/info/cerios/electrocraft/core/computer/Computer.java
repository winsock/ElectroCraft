package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraft.src.EntityPlayer;

import org.jpc.emulator.PC;
import org.jpc.emulator.pci.peripheral.DefaultVGACard;
import org.jpc.emulator.peripheral.Keyboard;
import org.jpc.j2se.VirtualClock;
import org.jpc.support.ArrayBackedSeekableIODevice;
import org.jpc.support.DriveSet;
import org.jpc.support.DriveSet.BootType;
import org.jpc.support.FloppyBlockDevice;
import org.jpc.support.TreeBlockDevice;

import cpw.mods.fml.common.FMLCommonHandler;

public class Computer implements Runnable {

	private volatile boolean running = false;
	private List<EntityPlayer> clients;
	private File baseDirectory;
	private PC internalComputer;
	private volatile boolean wasResumed = false;
	private Object sleepLock = new Object();
	private volatile boolean finishedSleeping = true;
	private Timer sleepTimer = new Timer();
	private List<Event> eventQueue = new ArrayList<Event>();
	private Object eventLock = new Object();
	private Object finalizeGuardian;
	private Thread thisThread;
	protected final ReentrantLock stateLock = new ReentrantLock();
	private volatile boolean ticked = false;

	public Computer(List<EntityPlayer> clients, String baseDirectory) {
		this.clients = clients;
		this.baseDirectory = new File(baseDirectory);
		if (!this.baseDirectory.exists()) {
			this.baseDirectory.mkdirs();
		}
		// Create a finalize guardian
		finalizeGuardian = new Object() {
			@Override
			public void finalize() {
				//shutdown();
			}
		};
		try {
			InputStream resource = this.getClass().getResourceAsStream("/info/cerios/electrocraft/images/floppy.img");
			FloppyBlockDevice floppy = new FloppyBlockDevice(new ArrayBackedSeekableIODevice("floppy", resource));
			TreeBlockDevice userFolder = new TreeBlockDevice(this.baseDirectory, true);
			DriveSet set = new DriveSet(BootType.FLOPPY, floppy, userFolder);
			internalComputer = new PC(new VirtualClock(), set);
			getVideoCard().resizeDisplay(720, 480);
		} catch (IOException e) {
			FMLCommonHandler.instance().raiseException(e, "Error making an internal JPC emulator", true);
		}
	}

	public List<EntityPlayer> getClients() {
		return clients;
	}

	public void removeClient(EntityPlayer client) {
		clients.remove(client);
	}

	public void addClient(EntityPlayer client) {
		clients.add(client);
	}

	public void tick() {
		if (!ticked)
			ticked = true;
	}

	public void callSave() {
//		try {
//			FileOutputStream fos = new FileOutputStream(new File(baseDirectory, ".state"));
//			internalComputer.saveState(fos);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void callLoad() {
//		File stateFile = new File(baseDirectory, ".state");
//		if (stateFile.exists()) {
//			stateLock.lock();
//			wasResumed = true;
//			stateLock.unlock();
//			FileInputStream fis;
//			try {
//				fis = new FileInputStream(stateFile);
//				internalComputer.loadState(fis);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		start();
	}

	public void shutdown() {
		running = false;
		sleepTimer.cancel();
		Thread.currentThread().interrupt();
		internalComputer.stop();
	}

	public void start() {
		if (!running) {
			stateLock.lock();
			running = true;
			stateLock.unlock();
			thisThread = new Thread(this);
			thisThread.start();
			internalComputer.start();
		}
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public synchronized boolean isRunning() {
		return running;
	}
	
	public DefaultVGACard getVideoCard() {
		return (DefaultVGACard) internalComputer.getComponent(DefaultVGACard.class);
	}

	public Keyboard getKeyboard() {
		return (Keyboard) internalComputer.getComponent(Keyboard.class);
	}

	public void registerNetworkBlock(NetworkBlock block) {
	}

	public void removeNetworkBlock(NetworkBlock block) {
	}

	@Override
	public void run() {
		// Register the Lua thread with the security manager
		//ElectroCraft.instance.getSecurityManager().registerThread(this);

		while (isRunning() && ElectroCraft.instance.isRunning()) {
			internalComputer.execute();
		}
		sleepTimer.cancel();
		this.running = false;
	}

	private class Event {
		public String eventName;
		public Object[] args;
	}
}
