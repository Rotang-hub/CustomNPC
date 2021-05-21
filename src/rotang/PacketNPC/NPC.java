package rotang.PacketNPC;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;

public class NPC
{
	private static List<EntityPlayer> npcs = new ArrayList<>();
	
	public static void createNPC(Player player, String skin)
	{
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
		GameProfile profile = new GameProfile(UUID.randomUUID(), skin);
		EntityPlayer npc = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
		
		Location pLoc = player.getLocation();
		npc.setLocation(pLoc.getX(), pLoc.getY(), pLoc.getZ(), pLoc.getYaw(), pLoc.getPitch());
		
		npc.getDataWatcher().set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 127);
		
		String[] name = getSkin(player, skin);
		profile.getProperties().put("textures", new Property("textures", name[0], name[1]));
	
		addNPCPacket(npc);
		npcs.add(npc);
		
		int var = 1;
		if(PacketNPC.getData().contains("data"))
			var = PacketNPC.getData().getConfigurationSection("data").getKeys(false).size() + 1;
		
		PacketNPC.getData().set("data." + var + ".x", (double) pLoc.getX());
		PacketNPC.getData().set("data." + var + ".y", (double) pLoc.getY());
		PacketNPC.getData().set("data." + var + ".z", (double) pLoc.getZ());
		PacketNPC.getData().set("data." + var + ".pitch", pLoc.getPitch());
		PacketNPC.getData().set("data." + var + ".yaw", pLoc.getYaw());
		PacketNPC.getData().set("data." + var + ".world", pLoc.getWorld().getName());
		PacketNPC.getData().set("data." + var + ".name", skin);
		PacketNPC.getData().set("data." + var + ".text", name[0]);
		PacketNPC.getData().set("data." + var + ".signature", name[1]);
		PacketNPC.saveData();
	}
	
	public static void loadNPC(Location location, GameProfile profile)
	{
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
		EntityPlayer npc = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
		
		npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		npc.getDataWatcher().set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 127);
	
		addNPCPacket(npc);
		npcs.add(npc);
	}
	
	public static void removeNPC(Player player, EntityPlayer npc)
	{
		npcs.remove(npc);
		npc.getBukkitEntity().remove();
		
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		
		connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
	}
	
	private static String[] getSkin(Player player, String name)
	{
		try 
		{
			URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
			InputStreamReader reader = new InputStreamReader(url.openStream());
			String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
			
			URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
			InputStreamReader reader2 = new InputStreamReader(url2.openStream());
			JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
			String texture = property.get("value").getAsString();
			String signature = property.get("signature").getAsString();
			
			return new String[] {texture, signature};
			
		} catch (Exception e) 
		{
			EntityPlayer p = ((CraftPlayer) player).getHandle();
			GameProfile profile = p.getProfile();
			Property property = profile.getProperties().get("textures").iterator().next();
			String texture = property.getValue();
			String signature = property.getSignature();
			
			return new String[] {texture, signature};
		}
	}
	
	public static void addNPCPacket(EntityPlayer npc)
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
			
			connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
			connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
			connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
			connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));
		}
	}
	
	public static void addJoinPacket(Player player)
	{
		for(EntityPlayer npc : npcs)
		{
			PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
			
			connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
			connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
			connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
			connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));
		}
	}
	
	public static List<EntityPlayer> getNPCs()
	{
		return npcs;
	}
}
