require 'java'

def main
	putc ">"
	loop do
		key = computer.getKeyboard.read;
		if key
			putc key
		end
		sleep(1/4)
	end
end

class KeyboardInput
	java_implements 'info.cerios.electrocraft.core.computer.IKeyboardInput'
	
	def onKeyTyped(line)
		if line == "help"
			puts "Welcome to CeriOS help!"
		end
	end
end

main();