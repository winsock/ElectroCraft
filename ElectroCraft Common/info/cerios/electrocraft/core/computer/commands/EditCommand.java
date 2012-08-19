package info.cerios.electrocraft.core.computer.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;

import info.cerios.electrocraft.core.computer.Computer;

public class EditCommand implements IComputerCommand {

	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		Map<Integer, String> fileLineMap = new HashMap<Integer, String>();
		int currentLine = 0;
		
		try {
			File file = new File(computer.getBaseDirectory().getAbsolutePath() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0]);
			if (!file.exists())
				file.createNewFile();
			
			computer.getTerminal().clear();
			computer.getTerminal().writeLine("Edit - Press Control - x to exit");
			computer.getTerminal().writeLine("SAVE BEFORE EXITING");
			computer.getTerminal().writeLine("Press Control - w to save");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			for(int i = 0; (line = br.readLine()) != null; i++) {
				computer.getTerminal().writeLine(line);
				if (!line.isEmpty())
					fileLineMap.put(i, line);
				currentLine = i;
			}
			br.close();
			
			char inputKey = '\0';

			while (true) {
				inputKey = computer.getKeyboard().waitForKey();
				
				if ((inputKey == 'x' && computer.getKeyboard().isCtrlDown()) || inputKey == 0x18) {
					break;
				} else if ((inputKey == 'w' && computer.getKeyboard().isCtrlDown()) || inputKey == 0x17) {
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					
					List<Integer> keys = new ArrayList<Integer>(fileLineMap.keySet());
					Collections.sort(keys);
					
					for (int i = 0; i < keys.size(); i++) {
						bw.write(fileLineMap.get(keys.get(i)));
						bw.write('\n');
					}
					
					bw.close();
				} else if (inputKey != '\0') {
					if (inputKey == '\n') {
							currentLine++;
					} else if (inputKey == '\b') {
						if (fileLineMap.get(currentLine).length() > 0) {
							fileLineMap.put(currentLine, fileLineMap.get(currentLine).substring(0, fileLineMap.get(currentLine).length() - 1));
							computer.getTerminal().deleteChar(true);
						}
					} else {
						if (fileLineMap.get(currentLine) == null || fileLineMap.get(currentLine).isEmpty())
							fileLineMap.put(currentLine, Character.toString(inputKey));
						else
							fileLineMap.put(currentLine, fileLineMap.get(currentLine) + inputKey);
					}
				}
			}
		} catch (IOException e) {
			computer.getTerminal().clear();
			try {
				computer.getTerminal().writeLine("Error! Could not open file for writting!");
			} catch (IOException e1) {
				FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft Computer: Edit Command, Fatal Error! Could not open or read file and cannot print error message to user!");
				computer.setRunning(false);
			}
		}
		
		computer.getTerminal().clear();
	}
}
