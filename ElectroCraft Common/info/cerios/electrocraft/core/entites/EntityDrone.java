package info.cerios.electrocraft.core.entites;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.IComputer;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.drone.Drone;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityLookHelper;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityDrone extends EntityLiving implements IComputer {

	private Drone drone;
	private String id = "";

	public EntityDrone(World par1World) {
		super(par1World);
		texture = "/info/cerios/electrocraft/gfx/Drone.png";
		this.height = 0.8F;
	}
	
	@Override
    public EntityLookHelper getLookHelper() {
		return super.getLookHelper();
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound var1) {
		super.readEntityFromNBT(var1);
		id = var1.getString("cid");
		createDrone();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound var1) {
		super.writeEntityToNBT(var1);
		var1.setString("cid", id);
	}

	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		if (!worldObj.isRemote && drone != null && drone.isRunning())
			drone.tick();
	}
	
	@Override
	public ItemStack getHeldItem() {
		return new ItemStack(Item.pickaxeDiamond, 1);
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	public boolean interact(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			if (drone == null)
				createDrone();
			if (drone.getLuaState() == null || !drone.getLuaState().isOpen()) {
				drone.loadLuaDefaults();
			}
			ElectroCraft.instance.setComputerForPlayer(player, this);
			GuiPacket guiPacket = new GuiPacket();
			guiPacket.setCloseWindow(false);
			guiPacket.setGui(Gui.COMPUTER_SCREEN);
			try {
				PacketDispatcher.sendPacketToPlayer(guiPacket.getMCPacket(), (Player) player);
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().severe("Unable to send \"Open Computer GUI Packet\"!");
			}
			if (!drone.isRunning()) {
				drone.setRunning(true);
				drone.loadBios();
				drone.postEvent("resume");
			}
			drone.addClient(player);
			return true;
		}
		return true;
	}
	
	public void createDrone() {
		if (!worldObj.isRemote) {
			String worldDir = "";
			if (FMLCommonHandler.instance().getSide() == Side.SERVER || FMLCommonHandler.instance().getSide() == Side.BUKKIT) {
				worldDir= worldObj.getWorldInfo().getWorldName();
			} else {
				worldDir = "saves" + File.separator + worldObj.getWorldInfo().getWorldName();
			}
			String dir = "";
			if (id == null || id.isEmpty()) {
				id = String.valueOf(Calendar.getInstance().getTime().getTime());
				dir = ElectroCraft.electroCraftSided.getBaseDir().getAbsolutePath() + File.separator + worldDir + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
				File file = new File(dir);
				while (file.exists()) {
					id = String.valueOf(Calendar.getInstance().getTime().getTime());
					dir = ElectroCraft.electroCraftSided.getBaseDir().getAbsolutePath() + File.separator + worldDir + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
					file = new File(dir);
				}
			}
			dir = ElectroCraft.electroCraftSided.getBaseDir().getAbsolutePath() + File.separator + worldDir + File.separator + "electrocraft" + File.separator + "computers" + File.separator + id;
			drone = new Drone(new ArrayList<EntityPlayer>(), "", dir, true, 320, 240, 15, 50);
			drone.setDrone(this);
		}
	}

	protected boolean canDespawn() {
		return false;
	}

	@Override
	public Computer getComputer() {
		return drone;
	}

	@Override
	public void removeActivePlayer(EntityPlayer player) {
		drone.removeClient(player);
	}

	@Override
	public int getMaxHealth() {
		return 100;
	}
}
