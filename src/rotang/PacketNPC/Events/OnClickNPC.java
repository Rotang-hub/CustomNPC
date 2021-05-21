package rotang.PacketNPC.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnClickNPC implements Listener
{
	@EventHandler
	public void onClickNPC(OnRightClickNPC event)
	{
		Player player = event.getPlayer();
		player.sendMessage("정신나갈거같애");
	}
}
