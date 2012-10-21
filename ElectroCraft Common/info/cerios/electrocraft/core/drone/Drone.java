package info.cerios.electrocraft.core.drone;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

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
	
	public Drone(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		super(clients, script, baseDirectory, isInternal, width, height, rows, columns);
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
	
	@Override
	public void loadBios() {
		super.loadBios();
		synchronized (luaStateLock) {
			try {
				luaState.load(this.getClass().getResourceAsStream("/info/cerios/electrocraft/rom/drone/dronebios.lua"), "drone_bios_" + this.getBaseDirectory().getName());
				luaState.newThread();
				luaState.setField(LuaState.REGISTRYINDEX, "electrocraft_coroutine_drone");
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().severe("Error loading drone BIOS!");
			}
		}
	}
	
	@Override
	public void loadLuaDefaults() {
		super.loadLuaDefaults();
		
		NamedJavaFunction[] droneAPI = new NamedJavaFunction[] {
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ForgeDirection dir;
						if (luaState.isNumber(-1)) {
							dir = ForgeDirection.getOrientation(luaState.checkInteger(-1));
						} else {
							dir = getDirection(drone.drone.rotationYaw);
						}
						drone.drone.moveEntity(dir.offsetX, dir.offsetY, dir.offsetZ);
						return 0;
					}

					@Override
					public String getName() {
						return "move";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						if (drone.drone.getRotationTicks() <= 0) {
							if (luaState.isNumber(-1)) {
								drone.drone.setDigDirection(ForgeDirection.getOrientation(luaState.checkInteger(-1)));
							}
							drone.drone.setRotationTicks(60);
							CustomPacket packet = new CustomPacket();
							packet.id = 5;
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(bos);
							try {
								dos.writeInt(drone.drone.entityId);
								dos.writeInt(drone.drone.getRotationTicks());
								packet.id = 5;
								packet.data = bos.toByteArray();
								PacketDispatcher.sendPacketToAllAround(drone.drone.posX, drone.drone.posY, drone.drone.posZ, 20, drone.drone.worldObj.provider.dimensionId, packet.getMCPacket());
							} catch (IOException e) {
								ElectroCraft.instance.getLogger().fine("Error sending tool use update to entity!");
							}
						}
						return 0;
					}

					@Override
					public String getName() {
						return "useTool";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						if (drone.drone.getInventory().getStackInSlot(-1) == null) {
							drone.drone.getInventory().setInventorySlotContents(luaState.checkInteger(-1), drone.drone.getInventory().getStackInSlot(luaState.checkInteger(-2)));
							drone.drone.getInventory().setInventorySlotContents(luaState.checkInteger(-2), null);
							luaState.pushBoolean(true);
						} else {
							luaState.pushBoolean(false);
						}
						return 1;
					}

					@Override
					public String getName() {
						return "moveStack";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ItemStack stack = drone.drone.getInventory().getStackInSlot(luaState.checkInteger(-2));
						int remainder = 0;
						if (stack.stackSize <= luaState.checkInteger(-1)) {
							remainder = stack.stackSize - luaState.checkInteger(-1);
							stack.stackSize = luaState.checkInteger(-1);
						} else {
							stack.stackSize = 0;
							remainder = luaState.checkInteger(-1);
						}
						if (drone.drone.getInventory().getStackInSlot(-2) == null) {
							drone.drone.getInventory().setInventorySlotContents(luaState.checkInteger(-2), stack);
							if (remainder > 0) {
								ItemStack newStack = stack.copy();
								newStack.stackSize = remainder;
								drone.drone.getInventory().setInventorySlotContents(luaState.checkInteger(-3), newStack);
							} else {
								drone.drone.getInventory().setInventorySlotContents(luaState.checkInteger(-3), null);
							}
							luaState.pushBoolean(true);
						} else {
							luaState.pushBoolean(false);
						}
						return 1;
					}

					@Override
					public String getName() {
						return "moveItems";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ForgeDirection dir;
						if (luaState.getTop() >= 2) {
							dir = ForgeDirection.getOrientation(luaState.checkInteger(-1));
						} else {
							dir = getDirection(drone.drone.rotationYaw);
						}
						
						int amount = 0;
						ItemStack stack = null;
						if (luaState.getTop() >= 3) {
							amount = luaState.checkInteger(-2);
							stack = drone.drone.getInventory().getStackInSlot(luaState.checkInteger(-2));
						} else {
							stack = drone.drone.getInventory().getStackInSlot(luaState.checkInteger(-2));
							if (stack != null) {
								amount = stack.stackSize;
							}
						}
						
						if (stack == null || amount <= 0) {
							luaState.pushBoolean(false);
							return 1;
						}
						
						int x = (int)(Math.floor(drone.getDrone().posX) + dir.offsetX);
						int y = (int)(Math.floor(drone.getDrone().posY) + dir.offsetY);
						int z = (int)(Math.floor(drone.getDrone().posZ) + dir.offsetZ);
						if (drone.drone.worldObj.getBlockTileEntity(x, y, z) != null && drone.drone.worldObj.getBlockTileEntity(x, y, z) instanceof IInventory) {
							IInventory inv = (IInventory) drone.drone.worldObj.getBlockTileEntity(x, y, z);
							if (inv instanceof ISidedInventory) {
								ISidedInventory sidedInv = (ISidedInventory) inv;
								if (!addToInventory(sidedInv, sidedInv.getStartInventorySide(dir.getOpposite()), sidedInv.getSizeInventorySide(dir.getOpposite()), stack)) {
									drone.drone.entityDropItem(stack, 0f);
								}
							} else {
								if (!addToInventory(inv, 0, inv.getSizeInventory(), stack)) {
									drone.drone.entityDropItem(stack, 0f);
								}
							}
						} else {
							drone.drone.entityDropItem(stack, 0f);
						}
						
						luaState.pushBoolean(true);
						return 1;
					}

					@Override
					public String getName() {
						return "drop";
					}
				}.init(this),
		};
		this.getLuaState().register("drone", droneAPI);
		luaState.pop(1);
		if (luaState != null && luaState.isOpen() && leftCard != null) {
			synchronized(luaStateLock) {
				luaState.register(leftCard.getValue1().getName(leftCard.getValue2()), leftCard.getValue1().getFunctions(leftCard.getValue2(), this));
				luaState.pop(1);
			}
		}
		if (luaState != null && luaState.isOpen() && rightCard != null) {
			synchronized(luaStateLock) {
				luaState.register(rightCard.getValue1().getName(rightCard.getValue2()), rightCard.getValue1().getFunctions(rightCard.getValue2(), this));
				luaState.pop(1);
			}
		}
	}
	
	public void setLeftCard(ICard card, ItemStack stack) {
		if (luaState != null && luaState.isOpen() && card == null && this.leftCard != null && (this.rightCard != null ? (this.rightCard.getValue1().getName(rightCard.getValue2()) != this.leftCard.getValue1().getName(leftCard.getValue2())) : true)) {
			synchronized(luaStateLock) {
				luaState.pushNil();
				luaState.setGlobal(leftCard.getValue1().getName(leftCard.getValue2()));
			}
		}
		if ((leftCard == null || (card != leftCard.getValue1() && luaState != null && luaState.isOpen())) && card != null) {
			synchronized(luaStateLock) {
				luaState.register(card.getName(stack), card.getFunctions(stack, this));
				luaState.setGlobal(card.getName(stack));
			}
		}
		this.leftCard = (card == null ? null : new ObjectPair<ICard, ItemStack>(card, stack));
	}
	
	public void setRightCard(ICard card, ItemStack stack) {
		if (luaState != null && luaState.isOpen() && card == null && this.rightCard != null && (this.leftCard != null ? (this.rightCard.getValue1().getName(rightCard.getValue2()) != this.leftCard.getValue1().getName(leftCard.getValue2())) : true)) {
			synchronized(luaStateLock) {
				luaState.pushNil();
				luaState.setGlobal(rightCard.getValue1().getName(rightCard.getValue2()));
			}
		}
		if ((rightCard == null || (card != rightCard.getValue1() && luaState != null && luaState.isOpen())) && card != null) {
			synchronized(luaStateLock) {
				luaState.register(card.getName(stack), card.getFunctions(stack, this));
				luaState.setGlobal(card.getName(stack));
			}
		}
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
		return MathHelper.floor_double((double)((rotation <= 0 ? (Math.abs(rotation) + 180) : (rotation)) * 4.0F / 360.0F) + 0.5D) & 3;
	}
	
	public ForgeDirection getDirection(float rotation) {
		return getDirection(getDir(rotation));
	}
	
	public ForgeDirection getDirection(int direction) {
		switch (direction) {
		case 0:
			return ForgeDirection.NORTH;
		case 1:
			return ForgeDirection.WEST;
		case 2:
			return ForgeDirection.SOUTH;
		case 3:
			return ForgeDirection.EAST;
		default:
			return ForgeDirection.UNKNOWN;
		}
	}
	
	public float getRotation(ForgeDirection direction) {
		switch (direction) {
		case NORTH:
			return 0f;
		case WEST:
			return 90f;
		case SOUTH:
			return 180f;
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
