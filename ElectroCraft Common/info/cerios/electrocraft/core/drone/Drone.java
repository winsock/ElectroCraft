package info.cerios.electrocraft.core.drone;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import cpw.mods.fml.common.network.PacketDispatcher;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Packet32EntityLook;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.api.utils.ObjectPair;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.drone.tools.SwordTool;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.CustomPacket;

public class Drone extends Computer {

	private EntityDrone drone;
	private boolean flying = false;
	private ObjectPair<ICard, ItemStack> leftCard;
	private ObjectPair<ICard, ItemStack> rightCard;

	public Drone(List<EntityPlayer> clients, String baseDirectory) {
		super(clients, baseDirectory);
	}

	@Override
	public void tick() {
		super.tick();
		if (leftCard != null)
			leftCard.getValue1().passiveFunctionTick(leftCard.getValue2());
		if (rightCard != null)
			rightCard.getValue1().passiveFunctionTick(rightCard.getValue2());
	}

	public void setDrone(EntityDrone drone) {
		this.drone = drone;
	}

	public EntityDrone getDrone() {
		return drone;
	}

	public void setLeftCard(ICard card, ItemStack stack) {
		this.leftCard = (card == null ? null : new ObjectPair<ICard, ItemStack>(card, stack));
	}

	public void setRightCard(ICard card, ItemStack stack) {
		this.rightCard = (card == null ? null : new ObjectPair<ICard, ItemStack>(card, stack));
	}

	public boolean getFlying() {
		return flying;
	}

	public void setFlying(boolean fly) {
		if (fly != flying) {
			CustomPacket packet = new CustomPacket();
			packet.id = 6;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			try {
				dos.writeInt(drone.entityId);
				dos.writeBoolean(fly);
				packet.id = 6;
				packet.data = bos.toByteArray();
				PacketDispatcher.sendPacketToAllAround(drone.posX, drone.posY, drone.posZ, 20, drone.worldObj.provider.dimensionId, packet.getMCPacket());
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().fine("Error sending tool use update to entity!");
			}
		}
		this.flying = fly;
	}

	public boolean addToInventory(IInventory inventory, int startIndex, int endIndex, ItemStack item) {
		for (int i = startIndex; i < endIndex; i++) {
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
					return true;
				}
			} else if (inventory.getStackInSlot(i) == null) {
				inventory.setInventorySlotContents(i, item);
				return true;
			}
		}
		return false;
	}

	public int getDir(float rotation) {
		return MathHelper.floor_double((double)(rotation * 4.0F / 360.0F) + 0.5D) & 3;
	}

	public ForgeDirection getDirection(float rotation) {
		return getDirection(getDir(rotation));
	}

	public ForgeDirection getDirection(int direction) {
		switch (direction) {
		case 0:
			return ForgeDirection.SOUTH;
		case 1:
			return ForgeDirection.WEST;
		case 2:
			return ForgeDirection.NORTH;
		case 3:
			return ForgeDirection.EAST;
		default:
			return ForgeDirection.UNKNOWN;
		}
	}

	public float getRotation(ForgeDirection direction) {
		switch (direction) {
		case NORTH:
			return 180f;
		case WEST:
			return 90f;
		case SOUTH:
			return 0f;
		case EAST:
			return 270f;
		default:
			return 0f;
		}
	}

	// Register tools
	static {
		ElectroCraft.instance.registerDroneTool(new SwordTool());
	}
}
