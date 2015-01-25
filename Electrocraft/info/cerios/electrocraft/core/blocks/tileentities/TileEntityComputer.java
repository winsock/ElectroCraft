package info.cerios.electrocraft.core.blocks.tileentities;

import info.cerios.electrocraft.api.IComputerHost;
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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.util.EnumFacing;

@ExposedToLua
public class TileEntityComputer extends NetworkBlock implements IUpdatePlayerListBox, IDirectionalBlock, IComputerHost {

    private Computer computer;
    private Set<NetworkBlock> ioPorts = new HashSet<NetworkBlock>();
    private List<EntityPlayer> activePlayers = new ArrayList<EntityPlayer>();
    private EnumFacing direction = EnumFacing.NORTH;
    private volatile boolean loadingState = false;

    /**
     * The default base directory for a new computer, its format is as follows
     * XYZEpochTime Where X = the x position of the block, Y = the y position of
     * the block, Z = the z position of the block EpochTime = Milliseconds since
     * the Unix Epoch
     */
    private String baseDirectory = "";
    private String id = "";

    public TileEntityComputer() {
        this.controlAddress = 4096;
        this.dataAddress = 4097;
        ioPorts.add(this);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setString("baseDirectory", baseDirectory);
        nbttagcompound.setInteger("direction", direction.ordinal());
        if (computer != null && computer.isRunning()) {
            nbttagcompound.setBoolean("isOn", computer.isRunning());

            computer.callSave();
            if (computer.getProgramStorage() != null) {
                nbttagcompound.setTag("programStorage", computer.getProgramStorage());
            }

            /*
             * Old code pertaining to persistence
             */
            // nbttagcompound.setInteger("openFileHandles",
            // computer.getNumberOfOpenFileHandles());
            // NBTTagList prevCommands = new NBTTagList();
            // for (String s : computer.getPreviousCommands()) {
            // prevCommands.appendTag(new NBTTagString("command", s));
            // }
            // nbttagcompound.setTag("prevCommands", prevCommands);
            //
            // NBTTagList terminal = new NBTTagList();
            // for (int i = 0; i < computer.getTerminal().getRows(); i++) {
            // Map<Integer, Character> row = computer.getTerminal().getRow(i);
            // NBTTagList tagRow = new NBTTagList();
            // for (int j = 0; j < computer.getTerminal().getColumns(); j++) {
            // tagRow.appendTag(new NBTTagString("" + j, row == null ? "" :
            // row.get(j) == null ? "" : row.get(j).toString()));
            // }
            // terminal.appendTag(tagRow);
            // }
            // nbttagcompound.setTag("terminal", terminal);
            // nbttagcompound.setInteger("row",
            // computer.getTerminal().getCurrentRow());
            // nbttagcompound.setInteger("col",
            // computer.getTerminal().getCurrentColumn());
            //
            // nbttagcompound.setByteArray("vga",
            // computer.getVideoCard().getData());
            // nbttagcompound.setBoolean("graphics",
            // computer.isInGraphicsMode());
            //
            // computer.saveCurrentState();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.baseDirectory = nbttagcompound.getString("baseDirectory");
        this.direction = EnumFacing.values()[nbttagcompound.getInteger("direction")];
        if (nbttagcompound.getBoolean("isOn") && !loadingState) {
            loadingState = true;
            if (computer == null) {
                createComputer();
            }
            if (nbttagcompound.hasKey("programStorage"))
                computer.setProgramStorage((NBTTagCompound) nbttagcompound.getTag("programStorage"));
            computer.callLoad();

            for (NetworkBlock ioPort : ioPorts) {
                computer.registerNetworkBlock(ioPort);
            }

            /*
             * Old code pertaining to persistence
             */
            // computer.setOpenFileHandles(nbttagcompound.getInteger("openFileHandles"));
            // List<String> commands = new ArrayList<String>();
            // NBTTagList prevCommands =
            // nbttagcompound.getTagList("prevCommands");
            // for (int i = 0; i < prevCommands.tagCount(); i++) {
            // commands.add(((NBTTagString)prevCommands.tagAt(i)).data);
            // }
            //
            // Map<Integer, Map<Integer, Character>> terminal = new
            // HashMap<Integer, Map<Integer, Character>>();
            // NBTTagList terminalTag = nbttagcompound.getTagList("terminal");
            // for (int i = 0; i < terminalTag.tagCount(); i++) {
            // Map<Integer, Character> row = new HashMap<Integer, Character>();
            // NBTTagList rowTag = (NBTTagList) terminalTag.tagAt(i);
            // for (int j = 0; j < rowTag.tagCount(); j++) {
            // row.put(j, ((NBTTagString)rowTag.tagAt(j)).data.length() <= 0 ?
            // null : ((NBTTagString)rowTag.tagAt(j)).data.charAt(0));
            // }
            // terminal.put(i, row);
            // }
            // computer.getTerminal().setTerminal(terminal);
            // computer.getTerminal().setPosition(nbttagcompound.getInteger("row"),
            // nbttagcompound.getInteger("col"));
            //
            // computer.getVideoCard().setData(nbttagcompound.getByteArray("vga"));
            // computer.setGraphicsMode(nbttagcompound.getBoolean("graphics"));
            //
            // new Thread(new Runnable() {
            // @Override
            // public void run() {
            // computer.loadState();
            // }
            // }).start();
        }
    }

    @Override
    public void update() {
        super.update();
        if (computer != null && computer.isRunning()) {
            computer.tick();
        }
    }

    @Override
    public Computer getComputer() {
        return computer;
    }

    public void createComputer() {
        if (worldObj == null || worldObj.isRemote)
            return;
        if (this.baseDirectory.isEmpty()) {
            this.id = String.valueOf(Math.abs(this.getPos().getX())) + String.valueOf(Math.abs(this.getPos().getY())) + String.valueOf(Math.abs(this.getPos().getZ())) + String.valueOf(Calendar.getInstance().getTime().getTime());
            File worldFolder = worldObj.getSaveHandler().getWorldDirectory();
            this.baseDirectory = worldFolder.getAbsolutePath() + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
        }
        computer = new Computer(activePlayers, baseDirectory, 320, 240, 15, 50);
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
            computer.shutdown();
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
    public void setDirection(EnumFacing direction) {
        this.direction = direction;
    }

    @Override
    public EnumFacing getDirection() {
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
