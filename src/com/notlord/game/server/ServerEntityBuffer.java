package com.notlord.game.server;

import com.notlord.Constants;
import com.notlord.ServerHandler;
import com.notlord.Window;
import com.notlord.game.Entity;
import com.notlord.math.AABB;
import com.notlord.math.Vector2f;
import com.notlord.networking.Server;
import com.notlord.networking.packets.EntityPositionPacket;
import com.notlord.networking.packets.EntityVisibilityPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerEntityBuffer {
	private final Map<Entity, List<EntityBufferEntry>> entities = new ConcurrentHashMap<>();
	private final float delay;
	private final ServerHandler serverHandler;
	private final Server server;

	public ServerEntityBuffer(ServerHandler serverHandler, float delay) {
		this.serverHandler = serverHandler;
		this.server = serverHandler.server;
		this.delay = delay;
	}

	private float timer = 0;
	private float timer2 = 0;

	public void next(float dt){
		timer += dt;
		timer2 += dt;
		Map<Entity, List<EntityBufferEntry>> entities = new HashMap<>(this.entities);
		if(timer >= delay) {
			for (Entity entity : entities.keySet()) {
				if (entities.get(entity) != null && (entities.get(entity).isEmpty() ||
						!entities.get(entity).get(entities.get(entity).size() - 1).position.equals(new Vector2f(entity.getX(), entity.getY())))) {
					entities.get(entity).add(0, new EntityBufferEntry(new Vector2f(entity.getX(), entity.getY())));
				}
			}
			while (timer >= delay){
				timer -= delay;
			}
		}
		if(timer2 >= Constants.SERVER_BUFFER_PLAYER_DELAY){
			//
			for(Player player : serverHandler.getAllPlayers()){
				if(player.getClientInstance() != null){
					player.getClientInstance().send(new EntityPositionPacket(player.id,new Vector2f(player.getX(), player.getY())));
				}
			}
			//
			while (timer2 >= Constants.SERVER_BUFFER_PLAYER_DELAY){
				timer2 -= Constants.SERVER_BUFFER_PLAYER_DELAY;
			}
		}

		for(Entity entity : entities.keySet()){
			List<EntityBufferEntry> list = new ArrayList<>(entities.get(entity));
			for(EntityBufferEntry entry : list){
				entry.dt += dt;
				if(entry.dt >= delay){
					if(entity.getX() != entry.position.x || entity.getY()  != entry.position.y) {
						if(entity instanceof Player player && !(entity instanceof ServerPlayer)){
							server.sendAllExclude(new EntityPositionPacket(entity.id, entry.position), player.getClientInstance());
						}
						else {
							for(Player player : serverHandler.getAllPlayers()){
								if(player instanceof ServerPlayer) continue;
								AABB aabb = new AABB(-Window.WIDTH/2f - 80, -Window.HEIGHT/2f - 80,
										Window.WIDTH/2f + 80, Window.HEIGHT/2f + 80);
								aabb.moveX(player.getX());
								aabb.moveY(player.getY());
								if(aabb.contains(entry.position)){
									player.getClientInstance().send(new EntityPositionPacket(entity.id, entry.position));
									if(!player.entitiesInFOV.contains(entity.id)){
										player.getClientInstance().send(new EntityVisibilityPacket(entity.id,true));
										player.entitiesInFOV.add(entity.id);
									}
								}
								else if(player.entitiesInFOV.contains(entity.id)){
									player.getClientInstance().send(new EntityVisibilityPacket(entity.id,false));
									player.entitiesInFOV.remove(entity.id);
								}
							}
						}
					}
					if(entities.get(entity) != null) {
						entities.get(entity).remove(entry);
					}
				}
			}
		}

	}

	public void addEntity(Entity e){
		entities.put(e, new CopyOnWriteArrayList<>());
	}

	public void remove(Entity e){
		entities.remove(e);
	}

	public List<Entity> getAllEntities(){
		return new ArrayList<>(entities.keySet());
	}

	public void clear(){
		entities.clear();
	}

	private static class EntityBufferEntry{
		public EntityBufferEntry(Vector2f position) {
			this.position = position;
			this.dt = 0;
		}

		public Vector2f position;
		public float dt;
	}
}
