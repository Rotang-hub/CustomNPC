package rotang.PacketNPC.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import rotang.PacketNPC.NPC;
import rotang.PacketNPC.PacketReader;

public class OnJoin implements Listener
{
	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		if(NPC.getNPCs() == null || NPC.getNPCs().isEmpty())
			return;
		
		NPC.addJoinPacket(player);
		
		PacketReader reader = new PacketReader();
		reader.inject(player);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		PacketReader reader = new PacketReader();
		reader.uninject(player);
	}
}
