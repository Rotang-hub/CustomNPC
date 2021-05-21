package rotang.PacketNPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandTab implements TabCompleter
{
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
	{
		List<String> arguments = Arrays.asList("create", "reload");
		List<String> result = new ArrayList<>();
		
		if(args.length == 1)
		{
			for(String a : arguments)
			{
				if(a.toLowerCase().startsWith(args[0].toLowerCase()))
				{
					result.add(a);
				}
			}
			return result;
		}
		
		return null;
	}
}
