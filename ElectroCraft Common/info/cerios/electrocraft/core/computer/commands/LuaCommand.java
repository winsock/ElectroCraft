package info.cerios.electrocraft.core.computer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaSyntaxException;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.luajavaapi.ComputerFile;
import info.cerios.electrocraft.core.computer.luajavaapi.ComputerSocket;

public class LuaCommand implements IComputerCommand {
		
	public LuaCommand() {
		
	}
	
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		try {
			ComputerFile file = new ComputerFile(computer.getBaseDirectory().getAbsolutePath() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0], computer);
			FileInputStream fis = new FileInputStream(file.getJavaFile());
			computer.getLuaState().load(fis, argv[0]);
			fis.close();
			computer.getLuaState().call(0, LuaState.MULTRET);
		} catch (LuaSyntaxException e) {
			computer.getTerminal().print("Error running lua script: Syntax Error!");
			computer.getTerminal().print(e.getLocalizedMessage());
		} catch (LuaRuntimeException e) {
			computer.getTerminal().print("Error running lua script: Runtime Error!");
			computer.getTerminal().print(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			computer.getTerminal().print("Lua file not found!");
		} catch (IOException e) {
			computer.getTerminal().print("Lua file not found!");
		}
		
		// Make sure that we are in terminal mode after running the program
		computer.setGraphicsMode(false);
	}
}