package chat;

import javax.jms.*;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by serb on 29.12.14.
 */
public class JmsP2Pchat implements MessageListener {

	private QueueConnection qConnection = null;
	private QueueSession qSession = null;
	private Queue responseQueue = null;
	private Queue requestQueue = null;
	private BufferedReader in = null;

	JmsP2Pchat(String queueConnFac, String requestQueue, String responseQueue) {
		this.in = new BufferedReader(new InputStreamReader(System.in));
		try {
			Context ctx = new InitialContext();
			QueueConnectionFactory qFactory = (QueueConnectionFactory) ctx
					.lookup(queueConnFac);
			qConnection = qFactory.createQueueConnection();
			qSession = qConnection.createQueueSession(false,
					Session.AUTO_ACKNOWLEDGE);
			this.requestQueue = (Queue) ctx.lookup(requestQueue);
			this.responseQueue = (Queue) ctx.lookup(responseQueue);
			qConnection.start();
		} catch (JMSException jmse) {
			jmse.printStackTrace();
			System.exit(1);
		} catch (NamingException jne) {
			jne.printStackTrace();
			System.exit(1);
		}
	}

	public void onMessage(Message message) {
		MapMessage msg = (MapMessage) message;
		try {
			System.out.print("received: " + msg.getString("message"));
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}

	}

	public void sendMessage(String message) {
		try {
			MapMessage msg = qSession.createMapMessage();
			msg.setString("message", message);
			msg.setJMSReplyTo(responseQueue);
			QueueSender qSender = qSession.createSender(requestQueue);
			qSender.send(msg);
		} catch (JMSException jmse) {
			jmse.printStackTrace();
			System.exit(1);
		}
	}

	private void exit() {
		try {
			qConnection.close();
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}
		System.exit(0);
	}

	public void run() {
		while (true) {
			try {
				String input = in.readLine();
				if (null != input) {
					if (input.trim().indexOf(":q!") == 0)
						exit();
					System.out.println("sent: " + input);
					sendMessage(input);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		JmsP2Pchat jmsP2Pchat = new JmsP2Pchat("", "", "");
		jmsP2Pchat.run();
	}
}
