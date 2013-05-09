package at.tomtasche.indoors.hipsterchat;

import java.util.LinkedList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;

public class User {

	protected static final String PROPERTY_JID = "jid";
	protected static final String PROPERTY_NAME = "name";
	protected static final String PROPERTY_BUSY = "busy";
	protected static final String PROPERTY_ROOMS = "rooms";

	private Entity entity;

	private String jid;
	private String name;
	private boolean busy;
	private List<String> rooms;

	public User() {
		entity = new Entity("User");

		rooms = new LinkedList<String>();
	}

	@SuppressWarnings("unchecked")
	public User(Entity entity) {
		if (entity == null)
			throw new IllegalArgumentException("entity must not be null");

		this.entity = entity;

		jid = (String) entity.getProperty(PROPERTY_JID);
		name = (String) entity.getProperty(PROPERTY_NAME);
		busy = (Boolean) entity.getProperty(PROPERTY_BUSY);
		rooms = (List<String>) entity.getProperty(PROPERTY_ROOMS);
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	public List<String> getRooms() {
		return rooms;
	}

	public void setRooms(List<String> rooms) {
		this.rooms = rooms;
	}

	public Entity toEntity() {
		entity.setProperty(PROPERTY_JID, jid);
		entity.setUnindexedProperty(PROPERTY_NAME, name);
		entity.setUnindexedProperty(PROPERTY_BUSY, busy);
		entity.setProperty(PROPERTY_ROOMS, rooms);

		return entity;
	}

	@Override
	public String toString() {
		return "User [jid=" + jid + ", name=" + name + ", busy=" + busy
				+ ", rooms=" + rooms + "]";
	}
}
