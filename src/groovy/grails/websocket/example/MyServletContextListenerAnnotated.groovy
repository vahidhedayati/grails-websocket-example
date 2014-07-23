package grails.websocket.example

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;


@WebListener
@ServerEndpoint("/annotated")
public class MyServletContextListenerAnnotated implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServerContainer serverContainer = (ServerContainer) servletContextEvent.getServletContext()
                                                    .getAttribute("javax.websocket.server.ServerContainer")

        try {
            serverContainer.addEndpoint(MyServletContextListenerAnnotated.class)
        } catch (DeploymentException e) {
            e.printStackTrace()
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
    @OnOpen
	public void handleOpen() { 
		System.out.println("Client is now connected.")
	}
	@OnMessage
	public String handleMessage(String message) {
		System.out.println("Client sent: " + message)
		String replyMessage = "echo "+message
		System.out.println("Send to Client: " + replyMessage)
		return replyMessage
	}
	@OnClose
	public void handeClose() { 
		System.out.println("Client is now disconnected.")
	}
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace()
	}
}
