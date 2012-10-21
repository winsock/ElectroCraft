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
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Packet32EntityLook;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
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
