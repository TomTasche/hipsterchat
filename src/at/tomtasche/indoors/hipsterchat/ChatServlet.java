package at.tomtasche.indoors.hipsterchat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@SuppressWarnings("serial")
public class ChatServlet extends HttpServlet {

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");
	private static final String[] COMMAND_PREFIXES = { "/", "--" };

	private XMPPService xmpp;
	private UserStore store;

	public ChatServlet() {
		xmpp = XMPPServiceFactory.getXMPPService();
		store = UserStore.getInstance();
	}

	// TODO: set users busy when they're unavailable
	// (/_ah/xmpp/presence/unavailable/)

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		Message message = xmpp.parseMessage(request);

		JID fromJid = message.getFromJid();
		String fromJidString = fromJid.getId().split("/")[0];
		JID toJid = message.getRecipientJids()[0];
		String room = toJid.getId().split("@")[0];
		String body = message.getBody();

		User fromUser = null;
		try {
			fromUser = store.getByJid(fromJidString);
		} catch (IllegalArgumentException e) {
		}

		if (containsCommand(body, "busy")) {
			fromUser.setBusy(true);
			store.update(fromUser);

			return;
		} else if (containsCommand(body, "unbusy")) {
			fromUser.setBusy(false);
			store.update(fromUser);

			return;
		} else if (containsCommand(body, "unsubscribe")) {
			store.delete(fromUser);

			return;
		} else if (containsCommand(body, "subscribe")) {
			store.add(fromJidString, fromJidString.split("@")[0], false, room);

			return;
		} else if (containsCommand(body, "list")) {
			StringBuilder builder = new StringBuilder();
			builder.append("users in room " + room + ":");
			builder.append(LINE_SEPARATOR);

			List<User> users = store.getByRoom(room);
			for (User user : users) {
				builder.append(user.getName());
				builder.append(" - ");
				builder.append(user.getJid());
				if (user.isBusy())
					builder.append(" (he's a busy boy at the moment)");

				builder.append(LINE_SEPARATOR);
			}

			body = builder.toString();

			sendMessage(body, toJid, fromJid);

			return;
		} else if (containsCommand(body, "name")) {
			String name = body.split(" ")[1];

			fromUser.setName(name);
			store.update(fromUser);

			return;
		} else if (containsCommand(body, "help")) {
			StringBuilder builder = new StringBuilder();
			builder.append("'/busy': set your user busy. you will no longer receive any messages until you '/unbusy' or until all users are reset (every day at midnight)");
			builder.append(LINE_SEPARATOR);
			builder.append("'/unbusy': set your user no longer busy. you will receive messages again. all users reset to 'unbusy' every day at midnight");
			builder.append(LINE_SEPARATOR);
			builder.append("'/unsubscribe': unsubscribe from this room. you will no longer receive any messages until you '/subscribe' again");
			builder.append(LINE_SEPARATOR);
			builder.append("'/subscribe': subscribe to this room. you will receive messages afterwards");
			builder.append(LINE_SEPARATOR);
			builder.append("'/list': list all users in this room");
			builder.append(LINE_SEPARATOR);
			builder.append("'/name': change your name. e.g. '/name AwesomeGuy'");
			builder.append(LINE_SEPARATOR);
			builder.append("'/help': get help.");

			body = builder.toString();

			sendMessage(body, toJid, fromJid);

			return;
		}

		if (fromUser == null)
			return;

		if (body.contains("OTR"))
			return;

		List<User> users = store.getByRoom(room);
		List<JID> jids = new LinkedList<JID>();
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			if (user.isBusy())
				continue;

			if (user.getJid().equals(fromJidString))
				continue;

			jids.add(new JID(user.getJid()));
		}

		JID[] jidsArray = new JID[jids.size()];
		jids.toArray(jidsArray);

		sendMessage("-- " + fromUser.getName() + ": " + LINE_SEPARATOR + body,
				toJid, jidsArray);
	}

	private boolean containsCommand(String body, String command) {
		for (String prefix : COMMAND_PREFIXES) {
			if (body.startsWith(prefix + command))
				return true;
		}

		return false;
	}

	private void sendMessage(String body, JID from, JID... to) {
		Message reply = new MessageBuilder().withFromJid(from).withBody(body)
				.withRecipientJids(to).build();
		xmpp.sendMessage(reply);
	}
}
