package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.api.IComputer;
import info.cerios.electrocraft.api.computer.ExposedToLua;
import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;

@ExposedToLua
public class TileEntityComputer extends NetworkBlock implements IDirectionalBlock, IComputer {

    private Computer computer;
    private Set<NetworkBlock> ioPorts = new HashSet<NetworkBlock>();
    private List<EntityPlayer> activePlayers = new ArrayList<EntityPlayer>();
    private ForgeDirection direction = ForgeDirection.NORTH;
    private volatile boolean loadingState = false;
    
    /**
     * The default base directory for a new computer, its format is as follows
     * XYZEpochTime
     * Where X = the x position of the block, Y = the y position of the block, Z = the z position of the block
     * EpochTime = Milliseconds since the Unix Epoch
     */
    private String baseDirectory = "";
    private String id = "";

    public TileEntityComputer() {
        this.controlAddress = 4096;
        this.dataAddress = 4097;
        ioPorts.add(this);
    }
    
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setString("baseDirectory", baseDirectory);
        nbttagcompound.setInteger("direction", direction.ordinal());
        if (computer != null && computer.isRunning()) {
            nbttagcompound.setBoolean("isOn", computer.isRunning());
            nbttagcompound.setString("currentDirectory", computer.getCurrentDirectory());
            nbttagcompound.setString("runningProgram", computer.getRunningProgram() == null ? "" : computer.getRunningProgram());
            
            computer.callSave();
            if (computer.getProgramStorage() != null)
            	nbttagcompound.setTag("programStorage", computer.getProgramStorage());
            
            /*
             * Old code pertaining to persistence
             */
//            nbttagcompound.setInteger("openFileHandles", computer.getNumberOfOpenFileHandles());
//            NBTTagList prevCommands = new NBTTagList();
//            for (String s : computer.getPreviousCommands()) {
//            	prevCommands.appendTag(new NBTTagString("command", s));
//            }
//            nbttagcompound.setTag("prevCommands", prevCommands);
//
//            NBTTagList terminal = new NBTTagList();
//            for (int i = 0; i < computer.getTerminal().getRows(); i++) {
//            	Map<Integer, Character> row = computer.getTerminal().getRow(i);
//            	NBTTagList tagRow = new NBTTagList();
//            	for (int j = 0; j < computer.getTerminal().getColumns(); j++) {
//            		tagRow.appendTag(new NBTTagString("" + j, row == null ? "" : row.get(j) == null ? "" : row.get(j).toString()));
//            	}
//            	terminal.appendTag(tagRow);
//            }
//            nbttagcompound.setTag("terminal", terminal);
//            nbttagcompound.setInteger("row", computer.getTerminal().getCurrentRow());
//            nbttagcompound.setInteger("col", computer.getTerminal().getCurrentColumn());
//            
//            nbttagcompound.setByteArray("vga", computer.getVideoCard().getData());
//            nbttagcompound.setBoolean("graphics", computer.isInGraphicsMode());
//            
//        	computer.saveCurrentState();
        }
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.baseDirectory = nbttagcompound.getString("baseDirectory");
        this.direction = ForgeDirection.values()[nbttagcompound.getInteger("direction")];
        if (nbttagcompound.getBoolean("isOn") && !loadingState) {
        	loadingState = true;
        	if (computer == null)
        		createComputer();
        	computer.setProgramStorage((NBTTagCompound) nbttagcompound.getTag("programStorage"));
        	computer.setRunning(true);
        	computer.callLoad();
        	        	
        	for (NetworkBlock ioPort : ioPorts) {
	    		computer.registerNetworkBlock(ioPort);
	    	}
        	
        	/*
             * Old code pertaining to persistence
             */
//        	computer.setOpenFileHandles(nbttagcompound.getInteger("openFileHandles"));
//        	List<String> commands = new ArrayList<String>();
//        	NBTTagList prevCommands = nbttagcompound.getTagList("prevCommands");
//        	for (int i = 0; i < prevCommands.tagCount(); i++) {
//        		commands.add(((NBTTagString)prevCommands.tagAt(i)).data);
//        	}
//        	
//        	Map<Integer, Map<Integer, Character>> terminal = new HashMap<Integer, Map<Integer, Character>>();
//        	NBTTagList terminalTag = nbttagcompound.getTagList("terminal");
//        	for (int i = 0; i < terminalTag.tagCount(); i++) {
//        		Map<Integer, Character> row = new HashMap<Integer, Character>();
//        		NBTTagList rowTag = (NBTTagList) terminalTag.tagAt(i);
//        		for (int j = 0; j < rowTag.tagCount(); j++) {
//        			row.put(j, ((NBTTagString)rowTag.tagAt(j)).data.length() <= 0 ? null : ((NBTTagString)rowTag.tagAt(j)).data.charAt(0));
//        		}
//        		terminal.put(i, row);
//        	}
//        	computer.getTerminal().setTerminal(terminal);
//        	computer.getTerminal().setPosition(nbttagcompound.getInteger("row"), nbttagcompound.getInteger("col"));
//        	
//        	computer.getVideoCard().setData(nbttagcompound.getByteArray("vga"));
//        	computer.setGraphicsMode(nbttagcompound.getBoolean("graphics"));
//        	
//	    	new Thread(new Runnable() {
//				@Override
//				public void run() {
//		        	computer.loadState();
//				}
//	    	}).start();
        }
    }
    
    public void updateEntity() {
    	super.updateEntity();
    	if (computer != null && computer.isRunning())
    		computer.tick();
    }
    
    @Override
    public Computer getComputer() {
    	return computer;
    }
    
    public void createComputer() {
    	if (worldObj == null || worldObj.isRemote) {
			computer = new Computer(activePlayers, "", baseDirectory, false, 320, 240, 15, 50);
			return;
    	}
    	if (this.baseDirectory.isEmpty()) {
    		String worldDir = "";
    		if (FMLCommonHandler.instance().getSide() == Side.SERVER || FMLCommonHandler.instance().getSide() == Side.BUKKIT) {
    			worldDir= worldObj.getWorldInfo().getWorldName();
    		} else {
    			worldDir = "saves" + File.separator + worldObj.getWorldInfo().getWorldName();
    		}
    		this.id = String.valueOf(Math.abs(this.xCoord)) + String.valueOf(Math.abs(this.yCoord)) + String.valueOf(Math.abs(this.zCoord)) + String.valueOf(Calendar.getInstance().getTime().getTime());
    		this.baseDirectory = ElectroCraft.electroCraftSided.getBaseDir().getAbsolutePath() + File.separator + worldDir + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
    	}
		computer = new Computer(activePlayers, "", baseDirectory, true, 320, 240, 15, 50);
    }
    
    @ExposedToLua
    public void startComputer() {
    	if (computer != null) {
	    	for (NetworkBlock ioPort : ioPorts) {
	    		computer.registerNetworkBlock(ioPort);
	    	}
	    	computer.start();
    	}
    }
    
    @ExposedToLua
    public void stopComputer() {
    	if (computer != null && computer.isRunning()) {
	    	computer.postEvent("kill");
    		computer.setRunning(false);
    	}
    }
    
    @ExposedToLua
    public String getId() {
    	return id;
    }

    public void addActivePlayer(EntityPlayer player) {
        this.activePlayers.add(player);
        if (this.computer != null) {
    		this.computer.addClient(player);
    	}
    }
    
    @Override
    public void removeActivePlayer(EntityPlayer player) {
    	this.activePlayers.remove(player);
    	if (this.computer != null) {
    		this.computer.removeClient(player);
    	}
    	ElectroCraft.instance.setComputerForPlayer(player, null);
    }

    public List<EntityPlayer> getActivePlayers() {
        return this.activePlayers;
    }
    
    public void registerIoPort(NetworkBlock block) {
    	if (computer != null && computer.isRunning()) {
    		computer.registerNetworkBlock(block);
		}
		ioPorts.add(block);
    }

    @Override
    public boolean canConnectNetwork(NetworkBlock block) {
        return true;
    }
    
	@Override
	public void setDirection(ForgeDirection direction) {
		this.direction = direction;
	}

	@Override
	public ForgeDirection getDirection() {
		return this.direction;
	}

	public void removeIoPort(NetworkBlock device) {
		if (computer != null && computer.isRunning()) {
    		computer.removeNetworkBlock(device);
		}
		ioPorts.remove(device);
	}

	@Override
	public void tick(Computer computer) {
	}
}
