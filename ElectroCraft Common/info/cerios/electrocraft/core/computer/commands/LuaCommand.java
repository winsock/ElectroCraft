package info.cerios.electrocraft.core.computer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaSyntaxException;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.luaapi.ComputerFile;
import info.cerios.electrocraft.core.computer.luaapi.ComputerSocket;

public class LuaCommand implements IComputerCommand {
		
	public LuaCommand() {
		
	}
	
	@Override
	public void onCommand(final Computer computer, int argc, String[] argv) {
		try {
			ComputerFile file = new ComputerFile(computer.getBaseDirectory().getAbsolutePath() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0], computer);
			FileInputStream fis = new FileInputStream(file.getJavaFile());
			if (!computer.getLuaState().isOpen())
				computer.loadLuaDefaults();
			computer.getLuaState().load(fis, argv[0]);
			computer.getLuaState().newThread();
			computer.getLuaState().setField(LuaState.REGISTRYINDEX, "electrocraft_program_coroutine");
			fis.close();
			computer.setRunningProgram(computer.getBaseDirectory().getAbsolutePath() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0]);
			
			// Resume the first time
			synchronized(Computer.luaStateLock) {
				computer.getLuaState().getField(LuaState.REGISTRYINDEX, "electrocraft_program_coroutine");
				computer.getLuaState().resume(-1, 0);
				computer.getLuaState().reset_kill();
				computer.getLuaState().pop(1);
			}
			while (computer.isRunning() && computer.getLuaState().isOpen()) {
				synchronized(Computer.luaStateLock) {
					computer.getLuaState().getField(LuaState.REGISTRYINDEX, "electrocraft_program_coroutine");
					if (computer.getLuaState().status(-1) == LuaState.YIELD) {
						computer.getLuaState().resume(-1, 0);
						computer.getLuaState().reset_kill();
					} else {
						computer.getLuaState().pop(1);
						break;
					}
					computer.getLuaState().pop(1);
				}
			}
		} catch (LuaSyntaxException e) {
			computer.getTerminal().print("Error running lua script: Syntax Error!");
			computer.getTerminal().print(e.getLocalizedMessage());
		} catch (LuaRuntimeException e) {
			computer.getTerminal().print("Error running lua script: Runtime Error!");
			computer.getTerminal().print("Possibly went to long without yielding?");
			computer.getTerminal().print(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			computer.getTerminal().print("Lua file not found!");
		} catch (IOException e) {
			computer.getTerminal().print("Lua file not found!");
		}
		
		computer.getLuaState().pushNil();
		computer.getLuaState().setField(LuaState.REGISTRYINDEX, "electrocraft_program_coroutine");
		computer.setRunningProgram(null);
		// Make sure that the kill switch is reset
		computer.getLuaState().reset_kill();
		// Make sure that we are in terminal mode after running the program
		computer.setGraphicsMode(false);
	}
}