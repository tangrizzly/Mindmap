var ws;

// code
// 1 : bindDocuemt
function message(code, data, msg) {
	this.code = code;
	this.data = data;
	this.msg = msg
}
function bindDocument(docId) {
	var message = {};
	message.code = 1;
	message.docId = docId;
	message.msg = "";
	
	ws.send(JSON.stringify(message));
	currentDocId = docId;
}
function handleMessage(message) {
	console.log(message);
	var msg = eval('('+message+')');
	console.log(msg);
	if(msg.code == 5){
		minder.importJson(msg.docData);
	}else if(msg.code == 2){
		var nodes = msg.nodes;
		for(n in nodes){
			var node = minder.getNodeById(nodes[n].data.id);
			if(node!=null){
				node.data = nodes[n].data;
				node.render();
			}else{
				var parent = minder.getNodeById(nodes[n].parent);
				var node = minder.createNode();
				node.data = nodes[n].data;
				parent.appendChild(node);
			}
			minder.refresh();
		}
	}
}
(function connect() {
	var addr = "ws://" + document.location.host + "/ws?token="+$.cookie('token');
	ws = new WebSocket(addr);
	ws.onopen = function(evt) {
		console.log("Connection open ...");
	};
	ws.onmessage = function(evt) {
		handleMessage(evt.data);
	};
	ws.onclose = function(evt) {
		console.log("Connection open ...");
	};
}())
