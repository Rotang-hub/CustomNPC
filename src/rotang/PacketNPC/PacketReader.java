package rotang.PacketNPC;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;
import rotang.PacketNPC.Events.OnRightClickNPC;

public class PacketReader 
{
	Channel channel;
	public static Map<UUID, Channel> channels = new HashMap<>();
	
	public void inject(Player player)
	{
		CraftPlayer craftPlayer = (CraftPlayer) player;
		channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
		channels.put(player.getUniqueId(), channel);
		
		if(channel.pipeline().get("PacketInjector") != null)
			return;
		
		channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<PacketPlayInUseEntity>(){
			@Override
			protected void decode(ChannelHandlerContext channel, PacketPlayInUseEntity packet, List<Object> arg) throws Exception 
			{
				arg.add(packet);
				readPacket(player, packet);
			}
		});
	}
	
	public void readPacket(Player player, Packet<?> packet)
	{
		if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity"))
		{
			if(getValue(packet, "action").toString().equalsIgnoreCase("ATTACK"))
				return;
			
			if(getValue(packet, "d").toString().equalsIgnoreCase("OFF_HAND"))
				return;
			
			if(getValue(packet, "action").toString().equalsIgnoreCase("INTERACT_AT"))
				return;
			
			int id = (int) getValue(packet, "a");
			
			if(getValue(packet, "action").toString().equalsIgnoreCase("INTERACT"))
			{
				for(EntityPlayer npc : NPC.getNPCs())
				{
					if(npc.getId() == id)
					{
						Bukkit.getScheduler().scheduleSyncDelayedTask(PacketNPC.getPlugin(PacketNPC.class), new Runnable() {
							@Override
							public void run() 
							{
								Bukkit.getPluginManager().callEvent(new OnRightClickNPC(player, npc));
							}
						}, 0);
					}
				}
			}
		}
	}
	
	public void uninject(Player player)
	{
		channel = channels.get(player.getUniqueId());
		if(channel.pipeline().get("PacketInjector") != null)
			channel.pipeline().remove("PacketInjector");
	}
	
	private Object getValue(Object instance, String name)
	{
		Object result = null;
		
		try 
		{
			Field field = instance.getClass().getDeclaredField(name);
			
			field.setAccessible(true);
			result = field.get(instance);
			field.setAccessible(false);
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return result;
	}
}
