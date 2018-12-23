angular.module('kityminderDemo', [ 'kityminderEditor' ])
// .config(function(configProvider) {
// configProvider.set('imageUpload', '../server/imageUpload.php');
// })
.controller('MainController', function($scope) {

	window.that = this;
	window.scope = $scope;
	$scope.initEditor = function(editor, minder) {
		window.editor = editor;
		window.minder = minder;
		minder.on('interactchange', function(e) {
			console.log(e);
			console.log('interact');
			if(currentDocId == -1){
				return;
			}
			console.log(e.minder._selectedNodes);
			currentNodes = e.minder._selectedNodes;
			if(currentNodes.length != 0){ // 有正在操作的结点
				if(JSON.stringify(lastContent) != JSON.stringify(minder.exportJson())){//操作的结点有变化
					
					pushRemote(false, currentNodes, minder.exportJson());
					lastContent = minder.exportJson();
				}
			}
		});
		minder.on('contentchange', function(e) {
			console.log(e);
			console.log('interact');
			if(currentDocId == -1){
				return;
			}
			console.log(e.minder._selectedNodes);
			currentNodes = e.minder._selectedNodes;
			if(currentNodes.length != 0){ // 有正在操作的结点
				if(JSON.stringify(lastContent) != JSON.stringify(minder.exportJson())){//操作的结点有变化
					
					pushRemote(false, currentNodes, minder.exportJson());
					lastContent = minder.exportJson();
				}
			}
		});
	};
});
var currentDocId = -1;
var rootDocId;
var lastContent = {};
function Node(doc) {
	this.doc = doc;
	this.next = [];
	this.searchById = searchById;
	function searchById(id) {
		if (doc.id == id) {
			return this;
		}
		for (n in this.next) {
			var d = this.next[n].searchById(id);
			if (d != null) {
				return d;
			}
		}
		return null;
	}
}

function findById(docs, id) {
	for (d in docs) {
		if (docs[d].id == id) {
			return docs[d];
		}
	}
	return null;
}

function getTreeData(docs) {
	var data = [];
	var trees = [];
	while (docs.length > 0) {
		var doc = docs.shift();
		var node = new Node(doc);
		while (true) {
			if (doc.parent == null) {
				trees.push(node);
				break;
			}
			var parent;
			for (t in trees) {
				parent = trees[t].searchById(doc.parent);
				if (parent != null) {
					break;
				}
			}
			if (parent != null) {
				parent.next.push(node);
				break;
			} else {
				var d = findById(docs, doc.parent);
				if (d == null) {
					trees.push(node);
					break;
				}
				var pnode = new Node(d);
				docs.splice(docs.indexOf(d), 1);
				pnode.next.push(node);
				node = pnode;
				doc = d;
			}
		}
	}
	addNodes(data, trees);
	console.log(JSON.stringify(data));
	return data;
}

function addNodes(children, nodes) {
	for (n in nodes) {
		var d = {};
		d.text = nodes[n].doc.name;
		d.href = nodes[n].doc
		if (nodes[n].doc.directory) {
			d.icon = "glyphicon glyphicon-book";
		} else {
			d.icon = "glyphicon glyphicon-file";
		}

		d.nodes = [];
		addNodes(d.nodes, nodes[n].next);
		if (d.nodes.length == 0) {
			d.nodes = null;
		}
		children.push(d);
	}
}

function getDocuments() {
	$.ajax({
		type : 'GET',
		url : '/document/doc?token=' + $.cookie('token'),
		success : function(data) {
			console.log(data);
			var documents;
			if (data.status == "success") {
				documents = data.data;
			} else {
				swal('错误', data.msg, 'error').then(function(result) {
					window.location.href = "/";
				});
				return;
			}
			$('#treeview').treeview({
				data : null
			});
			var tree1Data = getTreeData(documents.ownership.concat());
			rootDocId = tree1Data[0].href.id;
			$('#treeview1').treeview({
				data : tree1Data,
				highlightSelected : true,
				onNodeSelected : function(event, data) {
					console.log(data)
					if (data.href.directory) {
						return;
					}
					bindDocument(data.href.id, $.cookie('token'));
				}
			});
			$('#treeview2').treeview({
				data : getTreeData(documents.participation.concat()),
				onNodeSelected : function(event, data) {
					console.log(data)
					if (data.href.directory) {
						return;
					}
					bindDocument(data.href.id, $.cookie('token'));
				}
			});
		},
		error : function() {
			swal('错误', '请检查网络后重试，或联系管理员', 'error');
		}
	})
}
function handleCommit() {
	swal({
		title : '请输入备注',
		input : 'text',
		showCancelButton : true
	}).then(
			function(result) {
				console.log(result);
				if (result.value != null) {
					if (result.value == "") {
						swal('备注不能为空', '', 'error');
						return;
					}
					if (currentDocId == -1) {
						// 现在先返回错误，之后做一个提示新建文档
						swal('空的提交', '您还没有打开一个文档', 'error');
						return;
					}
					commit(currentDocId, JSON.stringify(minder.exportJson()),
							result.value);
				}
			})
}

function commit(documentId, content, comment) {
	$.ajax({
		type : 'POST',
		url : '/document/mitver?token=' + $.cookie('token'),
		dataType : 'JSON',
		contentType : 'application/json;charset=UTF-8',
		data : JSON.stringify({
			document : currentDocId,
			content : content,
			commiter : $.cookie('userId'),
			comment : comment
		}),
		success : function(response) {
			if(response.status == "success"){
				swal('提交成功', '版本名:' + response.data.name, 'success');
			}else{
				swal('提交失败', response.msg, 'error');
			}
		},
		error : function() {
			swal('错误', '请检查网络后重试，或联系管理员', 'error');
		}
	})
}
function exploreHistory(){
	$.ajax({
		type : 'GET',
		url : '/document/getvers?token=' + $.cookie('token')+"&did="+currentDocId,
		success : function(response) {
			if(response.status == "success"){
				console.log(response);
				var vers = response.data;
				var htmlStr = '';
				for(v in vers){
					htmlStr += '<tr><td>'+vers[v].name+'</td><td>'+vers[v].comment+'</td><td>'+vers[v].commitAt+'</td><td><input type="button" class="btn" value="回退" onclick="showHistory('+vers[v].id+');"</td></tr>'
				}
				swal({
	                title: '查看历史版本',
	                width: '800px',
	                html: '<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet"><script src="https://code.jquery.com/jquery.js"></script>      <!-- 包括所有已编译的插件 -->     <script src="js/bootstrap.min.js"></script><table class="table table-condensed"><tbody> '+
	                htmlStr+
	                '</tbody></table>',
	                preConfirm: function () {
	                    return new Promise(function (resolve) {
	                        resolve([
	                        ])
	                    })
	                },
	                onOpen: function () {
	                }
	            }).catch(swal.noop)
			}else{
				swal('提交失败', response.msg, 'error');
			}
		},
		error : function() {
			swal('错误', '请检查网络后重试，或联系管理员', 'error');
		}
	})
}
function showHistory(id){
	
	var message = {};
	message.code = 4;
	message.vid = id;
	message.docId = currentDocId;
	ws.send(JSON.stringify(message));
	
//	$.ajax({
//		type : 'GET',
//		url : '/document/getver?token=' + $.cookie('token')+'&vid='+id,
//		success : function(response) {
//			if(response.status == "success"){
////				minder.importJson(eval('('+response.data.content+')');
//				var data = eval('('+response.data.content+')');
//				minder.importJson(data);
//			}else{
//				swal('查看失败', response.msg, 'error');
//			}
//		},
//		error : function() {
//			swal('错误', '请检查网络后重试，或联系管理员', 'error');
//		}
//	})
}
$(document).ready(function() {
	$('#user-name').html($.cookie('user'));
	console.log($.cookie());
	$('#btnCommit').on('click', handleCommit);
	$('#btnInvite').on('click', createInvitation);
	$('#btnHistory').on('click', exploreHistory);
	getDocuments();
	$(".receiver").bind('DOMSubtreeModified', function(e) {
		if(currentDocId==-1){
			return;
		}
		var text = $(".receiver").val();
		console.log(text);
		if(text!=null&&text!=""){
			console.log(currentNodes[0]);
			currentNodes[0].setText();
			currentNodes[0].render();
		}
		console.log('changed');
	});
});

function getUserInfo() {
	window.location.href = "/user/info?token=" + $.cookie('token');
}
function createDocument(dir) {
	swal({
		title : '请输入名称',
		input : 'text',
		showCancelButton : true
	}).then(
			function(result) {
				console.log(result);
				if (result.value != null) {
					if (result.value == "") {
						swal('名称不能为空', '', 'error');
						return;
					}
					var s = $('#treeview1').treeview(true).getSelected()[0];
					var parentId;
					if (s) {
						var doc = s.href;
						if (doc.directory == false) {
							doc = $('#treeview1').treeview(true).getParent(
									s.nodeId).href;
						}
						parentId = doc.id;
					} else {
						parentId = rootDocId; // 默认在根目录下创建文档
					}
					$.ajax({
						type : 'POST',
						url : '/document?token=' + $.cookie('token'),
						dataType : 'JSON',
						contentType : 'application/json;charset=UTF-8',
						data : JSON.stringify({
							name : result.value,
							parent : parentId,
							directory : dir
						}),
						success : function(response) {
							console.log(response);
							swal('创建成功', '文档名：' + response.data.name,
									'success');
							getDocuments();
						},
						error : function() {
							swal('错误', '请检查网络后重试，或联系管理员', 'error');
						}
					})
				}
			});
}
// code: 2
// 节点变化
// code: 3
// 文字变化
function pushRemote(edit, nodes, data) {
	var message = {};
	message.docId = currentDocId;
	message.docData = data;
	if(!edit){
		message.code = 2;
		message.nodes = [];
		console.log(nodes);
		for(n in nodes){
			var node = {};
			node.data = nodes[n].data;
			if(nodes[n].parent!=null){
				node.parent = nodes[n].parent.data.id;				
			}
			message.nodes.push(node);
		}
	}else{
		return;
	}
	
	console.log(message);
	ws.send(JSON.stringify(message));
}
function createInvitation() {
	if(currentDocId == -1){
		return;
	}
	$.ajax({
		type : 'GET',
		url : '/document/invitation?token=' + $.cookie('token')+'&did='+currentDocId,
		success : function(response) {
			if(response.status == "success"){
				swal('成功生成邀请', '邀请码为: ' + response.data, 'success');
			}
		},
		error : function() {
			swal('错误', '请检查网络后重试，或联系管理员', 'error');
		}
	})
}
function fromInitation(){
	swal({
		title : '请输入邀请码',
		input : 'text',
		showCancelButton : true
	}).then(
			function(result) {
				console.log(result);
				if (result.value != null) {
					if (result.value == "") {
						swal('邀请码不能为空', '', 'error');
						return;
					}
					$.ajax({
						type : 'GET',
						url : '/document/addpart?utoken=' + $.cookie('token')+'&dtoken='+result.value,
						success : function(response) {
							console.log(response);
							swal('成功加入邀请', '文档名：' + response.data.name,
									'success');
							getDocuments();
						},
						error : function() {
							swal('错误', '请检查网络后重试，或联系管理员', 'error');
						}
					})
				}
			});
}
$(document).on('click', '#btnExport', function(event) {
	// inputOptions可以使一个object或者Promise
	var inputOptions = new Promise(function (resolve) {
	  setTimeout(function () {
	    resolve({
	      'json': 'json',
	      'txt': 'txt',
	      'svg': 'svg'
	    })
	  }, 500)
	})

	swal({
	  title: '选择导出类型',
	  input: 'radio',
	  inputOptions: inputOptions,
	  inputValidator: function (result) {
	    return new Promise(function (resolve, reject) {
	      if (result) {
	        resolve()
	      } else {
	        reject('你需要选择一项！')
	      }
	    })
	  }
	}).then(function (result) {
		event.preventDefault();
	    editor.minder.exportData(result.value).then(function(content){
	    	 var eleLink = document.createElement('a');
	    	    eleLink.download = "newfile."+result.value;
	    	    eleLink.style.display = 'none';
	    	    // 字符内容转变成blob地址
	    	    var blob = new Blob([content]);
	    	    eleLink.href = URL.createObjectURL(blob);
	    	    // 触发点击
	    	    document.body.appendChild(eleLink);
	    	    eleLink.click();
	    	    // 然后移除
	    	    document.body.removeChild(eleLink);
	    });
	});
});