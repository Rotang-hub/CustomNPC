package rotang.PacketNPC;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import rotang.PacketNPC.Events.OnClickNPC;
import rotang.PacketNPC.Events.OnJoin;

public class PacketNPC extends JavaPlugin
{
	public static DataManager data;
	
	@Override
	public void onEnable() 
	{
		data = new DataManager(this);
		
		getServer().getPluginManager().registerEvents(new OnJoin(), this);
		getServer().getPluginManager().registerEvents(new OnClickNPC(), this);
		getCommand("npc").setTabCompleter(new CommandTab());
		
		if(!Bukkit.getOnlinePlayers().isEmpty())
		{	
			for(Player player : Bukkit.getOnlinePlayers())
			{
				PacketReader reader = new PacketReader();
				reader.inject(player);
			}
		}
		
		if(data.getConfig().contains("data"))
			loadNPC();
	}
	
	@Override
	public void onDisable() 
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			PacketReader reader = new PacketReader();
			reader.uninject(player);
			
			for(EntityPlayer npc : NPC.getNPCs())
			{
				NPC.removeNPC(player, npc);
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		Player player = (Player) sender;
		
		if(label.equalsIgnoreCase("npc"))
		{
			if(args[0].equalsIgnoreCase("create"))
			{
				if(args.length == 1)
				{
					NPC.createNPC(player, player.getName());
					player.sendMessage("NPC Created");
					
					return true;
				}
				
				String name = args[1];
				NPC.createNPC(player, name);
				player.sendMessage(name + " Created");
			}
			
			if(args[0].equalsIgnoreCase("reload"))
			{
				for(Player online : Bukkit.getOnlinePlayers())
				{
					for(EntityPlayer npc : NPC.getNPCs())
					{
						NPC.removeNPC(online, npc);
					}
				}
				
				data = new DataManager(this);
				if(data.getConfig().contains("data"))
					loadNPC();
			}
			
			/*
			if(args[0].equalsIgnoreCase("remove"))
			{
				String name = args[1];
				NPC.removeNPC();
				player.sendMessage(name + " Removed");
			}*/
		}
		
		return false;
	}
	
	public static FileConfiguration getData()
	{
		return data.getConfig();
	}
	
	public static void saveData()
	{
		data.saveConfig();
	}
	
	public void loadNPC()
	{
		FileConfiguration file = data.getConfig();
		data.getConfig().getConfigurationSection("data").getKeys(false).forEach(npc -> {
			Location location = new Location(Bukkit.getWorld(file.getString("data." + npc + ".world")), 
					file.getDouble("data." + npc + ".x"),
					file.getDouble("data." + npc + ".y"),
					file.getDouble("data." + npc + ".z"));
			location.setPitch((float) file.getDouble("data." + npc + ".pitch"));
			location.setYaw((float) file.getDouble("data." + npc + ".yaw"));
			
			String name = file.getString("data." + npc + ".name");
			GameProfile profile = new GameProfile(UUID.randomUUID(), name);
			profile.getProperties().put("textures", new Property("textures", 
					file.getString("data." + npc + ".text"), file.getString("data." + npc + ".signature")));
			
			NPC.loadNPC(location, profile);
		});
	}
}
