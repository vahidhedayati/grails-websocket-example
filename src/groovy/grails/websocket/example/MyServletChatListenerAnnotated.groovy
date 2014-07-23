package grails.websocket.example

import grails.converters.JSON
import grails.web.JSONBuilder

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.DeploymentException
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint



@WebListener
@ServerEndpoint("/chatroomServerEndpoint")
public class MyServletChatListenerAnnotated implements ServletContextListener {

	static Set<Session> chatroomUsers = Collections.synchronizedSet(new HashSet<Session>())
	
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServerContainer serverContainer = (ServerContainer) servletContextEvent.getServletContext()
                                                    .getAttribute("javax.websocket.server.ServerContainer")

        try {
            serverContainer.addEndpoint(MyServletChatListenerAnnotated.class)
        } catch (DeploymentException e) {
            e.printStackTrace()
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
    @OnOpen
	public void handleOpen(Session userSession) { 
		chatroomUsers.add(userSession)
	}
	@OnMessage
	public String handleMessage(String message,Session userSession) throws IOException {
		def myMsg=[:]
		JSONBuilder jSON = new JSONBuilder ()
		String username=(String) userSession.getUserProperties().get("username")
		if (!username) {
			userSession.getUserProperties().put("username", message)
			myMsg.put("message", "System:connected as ==>"+message)
			def aa=myMsg as JSON
			userSession.getBasicRemote().sendText(aa as String)
		}else{
			Iterator<Session> iterator=chatroomUsers.iterator()
			myMsg.put("message", "${username}:${message}")
			def aa=myMsg as JSON
			while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(aa as String)
		}
	}
	@OnClose
	public void handeClose(Session userSession) { 
		chatroomUsers.remove(userSession)
	}
	@OnError
	public void handleError(Throwable t) {
		t.printStackTrace()
	}
	
}
