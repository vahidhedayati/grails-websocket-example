package grails.websocket.example

import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.slf4j.Logger
import org.slf4j.LoggerFactory



@WebListener
@ServerEndpoint("/annotated")
public class MyServletContextListenerAnnotated implements ServletContextListener {
	
	private final Logger log = LoggerFactory.getLogger(getClass().name)
	
    @Override
    public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.servletContext
		final ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")
		try {
			serverContainer.addEndpoint(MyServletContextListenerAnnotated)

			def ctx = servletContext.getAttribute(GA.APPLICATION_CONTEXT)

			def grailsApplication = ctx.grailsApplication

			def config = grailsApplication.config
			int defaultMaxSessionIdleTimeout = config.myservletcontext.timeout ?: 0
			serverContainer.defaultMaxSessionIdleTimeout = defaultMaxSessionIdleTimeout
		}
		catch (IOException e) {
			log.error e.message, e
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
