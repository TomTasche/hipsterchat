package at.tomtasche.indoors.hipsterchat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.Subscription;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@SuppressWarnings("serial")
public class SubscriptionServlet extends HttpServlet {

	private XMPPService xmpp;
	private UserStore store;

	public SubscriptionServlet() {
		xmpp = XMPPServiceFactory.getXMPPService();
		store = UserStore.getInstance();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Subscription subscription = xmpp.parseSubscription(request);

		String jid = subscription.getFromJid().getId().split("/")[0];
		String room = subscription.getToJid().getId().split("@")[0];

		store.add(jid, jid.split("@")[0], false, room);
	}
}
