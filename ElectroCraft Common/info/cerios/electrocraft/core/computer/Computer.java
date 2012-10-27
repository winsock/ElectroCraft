package info.cerios.electrocraft.core.computer;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraft.src.EntityPlayer;

public class Computer implements Runnable {

	private volatile boolean running = false;
	private SoundCard soundCard;
	private VideoCard videoCard;
	private Keyboard keyboard;
	private List<EntityPlayer> clients;
	private File baseDirectory;
	/**
	 * The current directory of the computer relative to the baseDirectory
	 */
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
		this.soundCard = new SoundCard();
		this.clients = clients;
		this.baseDirectory = new File(baseDirectory);
		if (!this.baseDirectory.exists()) {
			this.baseDirectory.mkdirs();
		}
		// Create a finalize guardian
		finalizeGuardian = new Object() {
			@Override
			public void finalize() {
				thisThread.interrupt();
			}
		};
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

	public void postEvent(String eventName, Object... args) {
		synchronized (eventLock) {
			Event e = new Event();
			e.eventName = eventName;
			e.args = args;
			eventQueue.add(e);
		}
	}

	protected void loadBios() {
	}

	public void callSave() {
		
	}

	public void callLoad() {
		if (running) {
			stateLock.lock();
			wasResumed = true;
			stateLock.unlock();
			thisThread = new Thread(this);
			thisThread.start();
		}
	}

	public void shutdown() {
		running = false;
		sleepTimer.cancel();
		Thread.currentThread().interrupt();
	}

	public void start() {
		if (!running) {
			stateLock.lock();
			running = true;
			stateLock.unlock();
			thisThread = new Thread(this);
			thisThread.start();
			postEvent("start");
		}
	}

	public void setRunning(boolean value) {
		stateLock.lock();
		running = value;
		if (value == false && thisThread != null)
			thisThread.interrupt();
		stateLock.unlock();
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public synchronized boolean isRunning() {
		return running;
	}
	
	public SoundCard getSoundCard() {
		return soundCard;
	}
	
	public VideoCard getVideoCard() {
		return videoCard;
	}

	public Keyboard getKeyboard() {
		return keyboard;
	}

	public void registerNetworkBlock(NetworkBlock block) {
	}

	public void removeNetworkBlock(NetworkBlock block) {
	}

	@Override
	public void run() {
		boolean first = true;
		// Register the Lua thread with the security manager
		ElectroCraft.instance.getSecurityManager().registerThread(this);

		int ticksSinceLastSave = 0;

		while (isRunning() && ElectroCraft.instance.isRunning()) {
			stateLock.lock();
			if (!isRunning() && ElectroCraft.instance.isRunning()) {
				stateLock.unlock();
				break;
			}

			if (!ticked) {
				stateLock.unlock();
				continue;
			}

			if (eventQueue.size() > 0) {
				List<Event> copy;
				synchronized (eventLock) {
					copy = new ArrayList<Event>(eventQueue);
					eventQueue.clear();
				}
			}
			stateLock.unlock();
		}
		sleepTimer.cancel();
		this.running = false;
	}

	private class Event {
		public String eventName;
		public Object[] args;
	}
}
