package at.tomtasche.indoors.hipsterchat;

import java.util.LinkedList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class UserStore {

	private static final UserStore INSTANCE = new UserStore();

	public static UserStore getInstance() {
		return INSTANCE;
	}

	private DatastoreService datastore;

	private UserStore() {
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	public synchronized User add(String jid, String name, boolean busy,
			String... rooms) {
		Entity entity = findByJid(jid);

		User user;
		if (entity == null) {
			user = new User();
		} else {
			user = new User(entity);
		}

		user.setJid(jid);
		user.setName(name);
		user.setBusy(busy);

		List<String> roomList = user.getRooms();
		for (String room : rooms) {
			if (!roomList.contains(room))
				roomList.add(room);
		}

		user.setRooms(roomList);

		datastore.put(user.toEntity());

		return user;
	}

	private Entity findByJid(String jid) {
		Query query = new Query("User");
		query.setFilter(new Query.FilterPredicate(User.PROPERTY_JID,
				FilterOperator.EQUAL, jid));

		return datastore.prepare(query).asSingleEntity();
	}

	public User getByJid(String jid) {
		return new User(findByJid(jid));
	}

	public synchronized List<User> getAll() {
		Query query = new Query("User");
		List<Entity> entities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());

		List<User> users = new LinkedList<User>();
		for (Entity entity : entities) {
			User user = new User(entity);
			users.add(user);
		}

		return users;
	}

	// FIXME: i'm ugly and inefficient...
	public synchronized List<User> getByRoom(String room) {
		Query query = new Query("User");
		List<Entity> entities = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());

		List<User> users = new LinkedList<User>();
		for (Entity entity : entities) {
			boolean inRoom = false;
			User user = new User(entity);

			for (String userRoom : user.getRooms()) {
				if (userRoom.equals(room)) {
					inRoom = true;

					break;
				}
			}

			if (inRoom)
				users.add(user);
		}

		return users;
	}

	public synchronized void update(User user) {
		datastore.put(user.toEntity());
	}

	public synchronized void delete(User user) {
		datastore.delete(user.toEntity().getKey());
	}
}
