This project is based on the example :

### WebSocket tutorial from ZA Software Development Tutorials

[Tutorial 01 - Java Server + JavaScript Client + GlassFish 4.0 + JDK 1.7](http://www.youtube.com/watch?v=_Fi4vz6oUio) + [WebSocket Tutorial 02 (Simple chatroom web app. - Java ServerEndpoint + JavaScript Client)](https://www.youtube.com/watch?v=BikL52HYaZg)


### Complete chat room product : [grails wschat plugin](http://grails.org/plugin/wschat) 
This original understanding was used to build a more advanced websocket chatroom plugin with multi room, admin user, popup (jquer-ui) private messaging, banning, kicking, blocking, popup (jquer-ui) webcam to multi users in a room and finally popup (jquer-ui) WebRTC (HD Audio/Video) between two people in a room. 


### Websocket Plugins under Grails 
[Websockets + SSH : used for live SSH Interaction](https://github.com/vahidhedayati/jssh)

[Websockets + HTTPBuilder: Parse Jenkins HTTP FrontEnd + push websocket feedback to Other Actions](https://github.com/vahidhedayati/grails-jenkins-plugin)

[Websockets + Chat + Websockets Chat client which could be used for automation ](https://github.com/vahidhedayati/grails-wschat-plugin)

[Websockets + Websockets Client used to query Grails domainClasses and produce select box through websockets client/server model = secure dynamic lookup ](https://github.com/vahidhedayati/grails-boselecta-plugin)


### Default Websockets with grails 3 and spring boot:
[testwebsocket-grails3](https://github.com/vahidhedayati/testwebsocket-grails3) This project has a brief explaination on how to configure a grails 3 application to interact with default websockets.


### Creating a basic chat echo system

In this project the above videos have been ported over to a Grails application running :

Grails 2.4.2 and JDK 1.7 

To recreate the project or where to go looking to see what has changed from a vanilla grails built application...

Please note : The javax.websocket is being provided by tomcat within BuildConfig.groovy:

```

build ":tomcat:7.0.52.1"
```

In order to get this app to work in production (Running in tomcat an additional dependency is required within dependencies of your BuildConfig usually found above plugins :
```
dependencies {
  build ('javax.websocket:javax.websocket-api:1.0') { export = false }
}
```

Take a look at the wschat BuildConfig.groovy to see it used.



So set up a new app :
```
grails create-app grails-websocket-example

cd grails-websocket-example
```

1. Setting up your Endpoints
Probably the hardest part of all of this was getting my head around how to get the endpoints loading in Grails.. 

1.1 first video:

This is the Java class converted to Groovy. It is your end point that when user sends a request it echos back user response to the user.. In the video the end point was something else I changed it to annotated


[MyServletContextListenerAnnotated.groovy](https://raw.githubusercontent.com/vahidhedayati/grails-websocket-example/master/src/groovy/grails/websocket/example/MyServletContextListenerAnnotated.groovy)

```groovy
package grails.websocket.example

import grails.util.Environment
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
			if (Environment.current == Environment.DEVELOPMENT) {
				serverContainer.addEndpoint(MyServletContextListenerAnnotated)
			}	

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
```

1.2 2nd Video Chat End Controller

This is your Listener / End point for the websocket chat room:


[MyServletChatListenerAnnotated.groovy](https://github.com/vahidhedayati/grails-websocket-example/blob/master/src/groovy/grails/websocket/example/MyServletChatListenerAnnotated.groovy)
```groovy
package grails.websocket.example

import grails.converters.JSON
import grails.web.JSONBuilder
import grails.util.Environment
import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerContainer
import javax.websocket.server.ServerEndpoint

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@WebListener
@ServerEndpoint("/chatroomServerEndpoint")
public class MyServletChatListenerAnnotated implements ServletContextListener {
	
	private final Logger log = LoggerFactory.getLogger(getClass().name)
	
	static final Set<Session> chatroomUsers = ([] as Set).asSynchronized()

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext = event.servletContext
		final ServerContainer serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer")
		try {
		
			if (Environment.current == Environment.DEVELOPMENT) {
				serverContainer.addEndpoint(MyServletChatListenerAnnotated)				
			}

			def ctx = servletContext.getAttribute(GA.APPLICATION_CONTEXT)

			def grailsApplication = ctx.grailsApplication

			def config = grailsApplication.config
			int defaultMaxSessionIdleTimeout = config.myservlet.timeout ?: 0
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

```


1.3 Registering your Endpoint listeners:

Inside your grails applications scripts folder place this _Events.groovy, it dynamically sets up both your listeners i.e. appends them to your web.xml as it builds the war file...

[scripts/_Events.groovy](https://raw.githubusercontent.com/vahidhedayati/grails-websocket-example/master/scripts/_Events.groovy)
```groovy
import groovy.xml.StreamingMarkupBuilder


eventWebXmlEnd = {String tmpfile ->
	
    def root = new XmlSlurper().parse(webXmlFile)
    root.appendNode {
       'listener' {
		   'listener-class' (
			   'grails.websocket.example.MyServletContextListenerAnnotated'
		   )
        }
    }
	root.appendNode {
		'listener' {
			'listener-class' (
				'grails.websocket.example.MyServletChatListenerAnnotated'
			)
		 }
	 }
    webXmlFile.text = new StreamingMarkupBuilder().bind {
        mkp.declareNamespace(
                "": "http://java.sun.com/xml/ns/javaee")
        mkp.yield(root)
    }
}

```

1.4 Registering your end point from within a plugin:

Refer to the wschat plugin link above and goto the Plugin descriptor file which has this block:

```groovy
def doWithWebDescriptor = { xml ->
		def listenerNode = xml.'listener'
		listenerNode[listenerNode.size() - 1] + {
			'listener' {
				'listener-class'(WsChatEndpoint.name)
			}
			'listener' {
				'listener-class'(WsCamEndpoint.name)
			}
	
		}
	}
```
	


2. Setting up your controller + views to access both 
```
grails create-controller grails.websocket.example.Test
vi grails-app/controllers/grails/websocket/example/TestController.groovy

```

2.1Have the following actions:

[TestController.groovy](https://raw.githubusercontent.com/vahidhedayati/grails-websocket-example/master/grails-app/controllers/grails/websocket/example/TestController.groovy)

```groovy
package grails.websocket.example

class TestController {

    def index() { }
	
	def chat() { }
	
}

```


inside grails-app/views/test create the following gsps:

[index.gsp](https://github.com/vahidhedayati/grails-websocket-example/blob/master/grails-app/views/test/index.gsp)
```gsp
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'admin.label', default: 'Admin')}" />
		<title><g:message code="default.create.label" args="[entityName,BAH,BAH]" /></title>
	</head>
	<body>	
	<form>
	<input id="textMessage" type="text">
	<input type="button" value="send" onClick="sendMessage();">
	</form>
	<br>
	<textarea id="messagesTextarea" rows="10" cols="50">
	</textarea>
	
	<script type="text/javascript">
		var webSocket=new WebSocket("ws://localhost:8080/grails-websocket-example/annotated");
		var messagesTextarea=document.getElementById("messagesTextarea");
		webSocket.onopen=function(message) {processOpen(message);};
		webSocket.onmessage=function(message) {processMessage(message);};
		webSocket.onclose=function(message) {processClose(message);};
		webSocket.onerror=function(message) {processError(message);};
		function processOpen(message) {
			messagesTextarea.value +=" Server Connect.... "+"\n";
		}
		function processMessage(message) {
			messagesTextarea.value +=" Receive from Server ===> "+ message.data +"\n";
		}
		function sendMessage() {
			
			if (textMessage.value!="close") {
				webSocket.send(textMessage.value);
				messagesTextarea.value +=" Send to Server ===> "+ textMessage.value +"\n";
				textMessage.value="";
			}else {
				websocket.close();
			}	
		}
		function processClose(message) {
			webSocket.send("Client disconnected......");
			messagesTextarea.value +="Server Disconnected... "+"\n";
		}
		function processError(message) {
			messagesTextarea.value +=" Error.... \n";
		}
	</script>
	</body>
	</html>
```

[chat.gsp](https://github.com/vahidhedayati/grails-websocket-example/blob/master/grails-app/views/test/chat.gsp)
```gsp
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'admin.label', default: 'Admin')}" />
		<title><g:message code="default.create.label" args="[entityName,BAH,BAH]" /></title>
	</head>
	<body>	
	<form>
	<input id="textMessage" type="text">
	<input type="button" value="send" onClick="sendMessage();">
	</form>
	<br>
	<textarea id="messagesTextarea" rows="10" cols="50">
	</textarea>
	
	<script type="text/javascript">
		var webSocket=new WebSocket("ws://localhost:8080/grails-websocket-example/chatroomServerEndpoint");
		var messagesTextarea=document.getElementById("messagesTextarea");
		webSocket.onmessage=function(message) {
			var jsonData=JSON.parse(message.data)
			if (jsonData.message!=null) {messagesTextarea.value +=jsonData.message+"\n";}
		}	
		webSocket.onclose=function(message) {processClose(message);};
		webSocket.onerror=function(message) {processError(message);};

		function processOpen(message) {
			messagesTextarea.value +=" Server Connect.... "+"\n";
		}

		function sendMessage() {
			if (textMessage.value!="close") {
				webSocket.send(textMessage.value);
				textMessage.value="";
			}else {
				websocket.close();
			}	
		}
		function processClose(message) {
			webSocket.send("Client disconnected......");
			messagesTextarea.value +="Server Disconnected... "+"\n";
		}
		function processError(message) {
			messagesTextarea.value +=" Error.... \n";
		}
	</script>
	</body>
	</html>

```


# Finished

grails run-app

now in test controller you will have two actions index which carries out what was done in tutorial 1's video 

http://localhost:8080/grails-websocket-example/test/chat - will give access to websocket chat server example as shown on 2nd video...

Please check out this as well : video https://www.youtube.com/watch?v=8QBdUcFqRkU 

### Please note  this is using Grails 2.4.2 with Java 1.7 - no additional libraries was required - Glasshfish etc not installed into grails.


