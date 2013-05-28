package at.tomtasche.indoors.hipsterchat;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class CronServlet extends HttpServlet {

	private UserStore store;

	public CronServlet() {
		store = UserStore.getInstance();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		List<User> users = store.getAll();
		for (User user : users) {
			if (user.isBusy()) {
				user.setBusy(false);

				store.update(user);
			}
		}
	}
}
