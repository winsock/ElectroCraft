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

import info.cerios.electrocraft.api.IComputerHost;
import info.cerios.electrocraft.api.computer.IComputerCallback;
import info.cerios.electrocraft.api.computer.IComputerRunnable;
import info.cerios.electrocraft.api.drone.tools.IDroneTool;
import info.cerios.electrocraft.core.ElectroCraft;
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
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

public class EntityDrone extends EntityLiving implements IComputerHost {

	private Drone drone;
	private String id = "";
	private InventoryDrone inventory;
	private boolean firstTick = true;
	private volatile int rotationTicks = 0;
	private ForgeDirection digDirection = ForgeDirection.UNKNOWN;
	private AbstractTool defaultTool = new AbstractTool();
	private volatile boolean clientFlying = false;

	public EntityDrone(World par1World) {
		super(par1World);
		texture = "/info/cerios/electrocraft/gfx/Drone.png";
		this.height = 0.8F;
		inventory = new InventoryDrone(this);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		if (worldObj.isRemote)
			return;
		id = nbt.getString("cid");
		createDrone();
		inventory.readFromNBT(nbt);
		if (drone != null) {
			drone.setFlying(nbt.getBoolean("flying"));
			if (nbt.getBoolean("isOn")) {
				drone.setRunning(true);
				drone.setProgramStorage(nbt.getCompoundTag("programStorage"));
				drone.callLoad();
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		if (worldObj.isRemote)
			return;
		inventory.writeToNBT(nbt);
		nbt.setString("cid", id);
		if (drone != null) {
			nbt.setBoolean("flying", drone.getFlying());
			nbt.setBoolean("isOn", drone.isRunning());

			drone.callSave();
			if (drone.getProgramStorage() != null)
				nbt.setTag("programStorage", drone.getProgramStorage());
		}
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
					packet.id = 7;
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
		if (drone != null && drone.isRunning()) {
			drone.tick();
		}
		if (rotationTicks > 0) {
			rotationTicks--;
			if (rotationTicks <= 0) {
				doToolAction();
			}
		}
	}

	@Override
	public void setDead() {
		this.dead = true;
		if (drone != null)
			drone.shutdown();
	}

	public void setClientFlying(boolean fly) {
		this.clientFlying = fly;
	}

	public void rotate(final float yaw, final int ticks) {
		newPosRotationIncrements = ticks;
		newPosX = posX;
		newPosY = posY;
		newPosZ = posZ;
		newRotationYaw = rotationYawHead = yaw;
		newRotationPitch = 0f;
	}

	public void move(final int x, final int y, final int z, final int ticks) {
		if (Block.blocksList[worldObj.getBlockId((int)posX + x, (int)posY + y, (int)posZ + z)] == null || Block.blocksList[worldObj.getBlockId((int)posX + x, (int)posY + y, (int)posZ + z)].isAirBlock(worldObj, (int)posX + x, (int)posY + y, (int)posZ + z)) {
			newPosRotationIncrements = ticks;
			newPosX = posX + x;
			newPosY = posY + y;
			newPosZ = posZ + z;
			newRotationYaw = rotationYawHead = rotationYaw;
			newRotationPitch = 0f;
		}
	}

	public boolean isStillMovingOrRotating() {
		return this.newPosRotationIncrements > 0;
	}

	@Override
	protected void fall(float par1) {
		if (worldObj.isRemote) {
			if (!clientFlying) {
				super.fall(par1);
			}
		} else if (drone == null || !drone.getFlying()) {
			super.fall(par1);
		}
	}

	@Override
	public boolean isOnLadder() {
		return false;
	}

	@Override
	public void moveEntityWithHeading(float par1, float par2) {
		if (worldObj.isRemote) {
			if (!clientFlying) {
				super.moveEntityWithHeading(par1, par2);
				return;
			}
		} else if (drone == null || !drone.getFlying()) {
			super.moveEntityWithHeading(par1, par2);
			return;
		}
		// Flying code from EntityFlying
		if (this.isInWater()) {
			this.moveFlying(par1, par2, 0.02F);
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929D;
			this.motionY *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		}
		else if (this.handleLavaMovement()) {
			this.moveFlying(par1, par2, 0.02F);
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		}
		else {
			float var3 = 0.91F;

			if (this.onGround) {
				var3 = 0.54600006F;
				int var4 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));

				if (var4 > 0) {
					var3 = Block.blocksList[var4].slipperiness * 0.91F;
				}
			}

			float var8 = 0.16277136F / (var3 * var3 * var3);
			this.moveFlying(par1, par2, this.onGround ? 0.1F * var8 : 0.02F);
			var3 = 0.91F;

			if (this.onGround) {
				var3 = 0.54600006F;
				int var5 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));

				if (var5 > 0) {
					var3 = Block.blocksList[var5].slipperiness * 0.91F;
				}
			}

			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= (double)var3;
			this.motionY *= (double)var3;
			this.motionZ *= (double)var3;
		}

		this.prevLegYaw = this.legYaw;
		double var10 = this.posX - this.prevPosX;
		double var9 = this.posZ - this.prevPosZ;
		float var7 = MathHelper.sqrt_double(var10 * var10 + var9 * var9) * 4.0F;

		if (var7 > 1.0F)
		{
			var7 = 1.0F;
		}

		this.legYaw += (var7 - this.legYaw) * 0.4F;
		this.legSwing += this.legYaw;
	}

	public Drone getDrone() {
		return drone;
	}

	public void doToolAction() {
		if (getHeldItem() == null)
			return;
		
		ForgeDirection dir = drone.getDirection(rotationYaw);
		if (digDirection != ForgeDirection.UNKNOWN) {
			dir = digDirection;
			digDirection = ForgeDirection.UNKNOWN;
		}

		int x = (int)(Math.floor(posX) + dir.offsetX);
		int y = (int)(Math.floor(posY) + dir.offsetY);
		int z = (int)(Math.floor(posZ) + dir.offsetZ);

		IDroneTool tool = defaultTool;

		for (IDroneTool t : ElectroCraft.instance.getDroneTools()) {
			if (t.isRightTool(getHeldItem())) {
				tool = t;
				break;
			}
		}

		if (tool.appliesToBlock(getHeldItem(), Block.blocksList[worldObj.getBlockId(x, y, z)], worldObj.getBlockMetadata(x, y, z))){
			for (ItemStack item : tool.preformAction(getHeldItem(), this, worldObj, x, y, z)) {
				drone.addToInventory(inventory, 0, 36, item);
			}
			tool.damageItem(this, getHeldItem(), Block.blocksList[worldObj.getBlockId(x, y, z)], worldObj.getBlockMetadata(x, y, z));
			drone.postEvent("tool", true);
		} else {
			drone.postEvent("tool", false);
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
				GuiPacket guiPacket = new GuiPacket();
				guiPacket.setCloseWindow(false);
				guiPacket.setGui(Gui.COMPUTER_SCREEN);
				try {
					PacketDispatcher.sendPacketToPlayer(guiPacket.getMCPacket(), (Player) player);
				} catch (IOException e) {
					ElectroCraft.instance.getLogger().severe("Unable to send \"Open Computer GUI Packet\"!");
				}
				if (!drone.isRunning()) {
					drone.start();
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
