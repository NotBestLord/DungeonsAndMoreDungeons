package com.notlord.game.server;

import com.notlord.Constants;
import com.notlord.Main;
import com.notlord.game.Entity;
import com.notlord.game.abilities.AbilityTreeNode;
import com.notlord.gui.interaction.PopText;
import com.notlord.math.AABB;
import com.notlord.math.Intersections;
import com.notlord.networking.packets.PopTextPacket;
import com.notlord.utils.Tuple;

import java.awt.*;
import java.util.List;

public class PlayerDamageBox extends DamageBox{
	private final Player owner;
	public PlayerDamageBox(DamageSource source, float damage, AABB boundingBox, float duration, Player owner) {
		super(source, damage, boundingBox, duration, owner);
		this.owner = owner;
	}

	@Override
	public void nextTick(float deltaTime) {
		timer += deltaTime;
		List<Entity> entities = Main.serverHandler.getEntities();
		for(Entity entity : entities){
			if(entity.getBoundingBox() != null && Intersections.AABBIntersectsAABB(entity.getBoundingBox(), boundingBox) && !damagedEntities.contains(entity.getId()) &&
					DamageSource.doesDamageApply(entity, source)){
				float d = damage;
				float f = 1;
				for (AbilityTreeNode node : owner.abilityTree.getNodes()) {
					if(node.isOwned()){
						Tuple<Float, Boolean> t = node.event.dealDamage(owner, entity);
						if(t.getK()) {
							f = 0;
							break;
						}
						f += t.getV();
					}
				}
				if(f == 0) continue;
				d *= f;
				entity.causeDamage(owner, d);
				damagedEntities.add(entity.getId());
				PopText txt = new PopText(new Font(Constants.DEFAULT_FONT.getName(),Font.BOLD, 18),Color.red,String.format("%.1f", d),
						entity.getBoundingBox().getMiddle().x,entity.getBoundingBox().getMiddle().y, 32,2.5f,true,"");
				Main.getPanel().addObject(txt);
				Main.serverHandler.server.sendAll(new PopTextPacket(txt, ""));
			}
		}
		if(timer >= duration){
			Main.serverHandler.removeDamageBox(this);
		}
	}
}
