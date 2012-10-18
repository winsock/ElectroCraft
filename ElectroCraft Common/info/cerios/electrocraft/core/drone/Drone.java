package info.cerios.electrocraft.core.drone;

import java.util.List;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import net.minecraft.src.EntityPlayer;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.entites.EntityDrone;

public class Drone extends Computer {
	
	private EntityDrone drone;
	
	public Drone(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		super(clients, script, baseDirectory, isInternal, width, height, rows, columns);
	}
	
	public void setDrone(EntityDrone drone) {
		this.drone = drone;
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
						luaState.pushBoolean(drone.drone.getNavigator().tryMoveToXYZ(luaState.checkNumber(-4), luaState.checkNumber(-3), luaState.checkNumber(-2), (float) luaState.checkNumber(-1)));
						return 1;
					}

					@Override
					public String getName() {
						return "move";
					}
				}.init(this),
		};
		this.getLuaState().register("drone", droneAPI);
	}
	
	
}
