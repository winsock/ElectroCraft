package info.cerios.electrocraft.core.entites;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import info.cerios.electrocraft.core.drone.InventoryDrone;
import info.cerios.electrocraft.core.drone.tools.AbstractTool;
import info.cerios.electrocraft.core.network.CustomPacket;
import info.cerios.electrocraft.core.network.GuiPacket;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityLookHelper;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

public class EntityDrone extends EntityLiving implements IComputer {

	private Drone drone;
	private String id = "";
	private InventoryDrone inventory;
	private boolean firstTick = true;
	private int rotationTicks = 0;
	private ForgeDirection digDirection = ForgeDirection.UNKNOWN;
	private AbstractTool defaultTool = new AbstractTool();

	public EntityDrone(World par1World) {
		super(par1World);
		texture = "/info/cerios/electrocraft/gfx/Drone.png";
		this.height = 0.8F;
		inventory = new InventoryDrone(this);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		inventory.readFromNBT(nbt);
		id = nbt.getString("cid");
		createDrone();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		inventory.writeToNBT(nbt);
		nbt.setString("cid", id);
	}

	public InventoryDrone getInventory() {
		return inventory;
	}

	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		if (firstTick) {
			if (worldObj.isRemote) {
				CustomPacket packet = new CustomPacket();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				try {
					dos.writeInt(entityId);
					packet.id = 4;
					packet.data = bos.toByteArray();
					PacketDispatcher.sendPacketToServer(packet.getMCPacket());
				} catch (IOException e) {
					ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
				}
			}
			firstTick = false;
		}
		if (worldObj.isRemote) {
			if (rotationTicks > 0) {
				rotationTicks--;
			}
			return;
		}
		if (drone != null && drone.isRunning())
			drone.tick();
		if (rotationTicks > 0) {
			rotationTicks--;
			if (rotationTicks <= 0) {
				doToolAction();
			}
		}
	}

	public void doToolAction() {
		ForgeDirection dir = drone.getDirection(rotationYaw);
		if (digDirection != ForgeDirection.UNKNOWN) {
			dir = digDirection;
			digDirection = ForgeDirection.UNKNOWN;
		}
		
		int x = (int)(Math.floor(posX) + dir.offsetX);
		int y = (int)(Math.floor(posY) + dir.offsetY);
		int z = (int)(Math.floor(posZ) + dir.offsetZ);

		if (defaultTool.appliesToBlock(getHeldItem(), Block.blocksList[worldObj.getBlockId(x, y, z)], worldObj.getBlockMetadata(x, y, z))){
			for (ItemStack item : defaultTool.preformAction(this, worldObj, x, y, z)) {
				addToInventory(item);
			}
			defaultTool.damageItem(this, getHeldItem(), Block.blocksList[worldObj.getBlockId(x, y, z)], worldObj.getBlockMetadata(x, y, z));
		}
	}

	private void addToInventory(ItemStack item) {
		for (int i = 0; i < 36; i++) {
			if (inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).itemID == item.itemID) {
				if (inventory.getStackInSlot(i).stackSize + item.stackSize > item.getMaxStackSize()) {
					int totalAmount = inventory.getStackInSlot(i).stackSize + item.stackSize;
					item.stackSize = item.getMaxStackSize();
					totalAmount -= item.stackSize;
					inventory.setInventorySlotContents(i, item);
					item.stackSize = totalAmount;
				} else {
					item.stackSize += inventory.getStackInSlot(i).stackSize;
					inventory.setInventorySlotContents(i, item);
					return;
				}
			} else if (inventory.getStackInSlot(i) == null) {
				inventory.setInventorySlotContents(i, item);
				return;
			}
		}
	}

	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z) {
		int id = world.getBlockId(x, y, z);
		Block block = Block.blocksList[id];
		int metadata = world.getBlockMetadata(x, y, z);
		return block.getBlockDropped(world, x, y, z, metadata, 0);
	}

	@Override
	public ItemStack getHeldItem() {
		return inventory.tools[1];
	}

	public int getRotationTicks() {
		return rotationTicks;
	}

	public void setRotationTicks(int ticks) {
		this.rotationTicks = ticks;
	}
	
	public void setDigDirection(ForgeDirection dir) {
		this.digDirection = dir;
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	public boolean interact(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			ElectroCraft.instance.setComputerForPlayer(player, this);
			if (player.isSneaking()) {
				player.openGui(ElectroCraft.instance, this.entityId, worldObj, (int)posX, (int)posY, (int)posZ);
			} else {
				if (drone == null)
					createDrone();
				if (drone.getLuaState() == null || !drone.getLuaState().isOpen()) {
					drone.loadLuaDefaults();
				}
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
					drone.postEvent("start");
				}
				drone.addClient(player);
			}
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
		if (drone != null)
			drone.removeClient(player);
		ElectroCraft.instance.setComputerForPlayer(player, null);
	}

	@Override
	public int getMaxHealth() {
		return 100;
	}
}
