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