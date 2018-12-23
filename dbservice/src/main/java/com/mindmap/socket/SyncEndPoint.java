package com.mindmap.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mindmap.config.SyncEndPointConfigure;
import com.mindmap.model.Document;
import com.mindmap.repository.DocumentRepository;
import com.mindmap.repository.VersionRepository;
import com.mindmap.token.TokenUtil;

@ServerEndpoint(value = "/ws", configurator = SyncEndPointConfigure.class)
@Component
public class SyncEndPoint {
	private final static Logger logger = LoggerFactory.getLogger(SyncEndPoint.class);

	public static Map<Integer, Set<Integer>> groups = new HashMap<>();
	public static Map<Integer, Session> users = new HashMap<>();
	public static Map<Integer, String> docs = new HashMap<>();
	@Autowired
	DocumentRepository documentRepository;
	@Autowired
	VersionRepository versionRepository;

	@OnOpen
	public void onOpen(Session session) {
		String token = session.getRequestParameterMap().get("token").get(0);
		try {
			Integer uid = TokenUtil.validateToken(token, "token");
			users.put(uid, session);
			logger.info("新用户连接：用户id" + uid);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		String token = session.getRequestParameterMap().get("token").get(0);
		logger.info(message);
		try {
			Integer uid = TokenUtil.validateToken(token, "token");
			JsonObject msg = (JsonObject) new JsonParser().parse(message);
			Integer code = msg.get("code").getAsInt();
			if (code == 1) {// 用户进组
				String content = bindDocument(uid, msg.get("docId").getAsInt());
				if (content != null) {
					sendTextMessage(session, "{code:5,docData:" + content + ",msg:null}");
				} else {
					sendTextMessage(session, "{code:10,data:{content:null},msg:null}");
				}
			} else if(code == 4){
				Integer vid = msg.get("vid").getAsInt();
				Integer docId = msg.get("docId").getAsInt();
				String content = rollback(docId, vid);
				for(Integer us : groups.get(docId)) {
					sendTextMessage(users.get(us), "{code:5,docData:" + content + ",msg:null}");
				}
			}
			else {
				docs.put(msg.get("docId").getAsInt(), msg.get("docData").getAsJsonObject().toString());
				broadCast(uid,msg.get("docId").getAsInt(), message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

	@OnClose
	public void onClose(Session session) {
		String token = session.getRequestParameterMap().get("token").get(0);
		try {
			Integer uid = TokenUtil.validateToken(token, "token");
			users.remove(uid);
			remoteUserFromGroupIfExits(uid);
			logger.info("用户退出：用户id" + uid);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	private void sendTextMessage(Session session, String message) {
		try {
			session.getBasicRemote().sendText(message);
			logger.info("message sent: " + message);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	private void broadCast(Integer uid, Integer did, String message) {
		Set<Integer> userIds = groups.get(did);
		for(Integer userId: userIds) {
			if(userId != uid) {
				sendTextMessage(users.get(userId), message);
			}
		}
	}
	private String rollback(Integer did, Integer vid) {
		String content = "";
		try {
			Document doc = documentRepository.findById(did).get();
			doc.setLatestVersion(vid);
			documentRepository.save(doc);
			content = versionRepository.findById(vid).get().getContent();
			docs.put(did, content);
			return content;
		}catch (Exception e) {
			logger.error(e.getMessage());
		}
		return content;
	}
	private void remoteUserFromGroupIfExits(Integer uid) {
		for (Entry<Integer, Set<Integer>> group : groups.entrySet()) {
			Set<Integer> usersId = (Set<Integer>)group.getValue();
			if (usersId.contains(uid)) {
				usersId.remove(uid);
				if(users.size()==0) { // 移除空的组以组本组里的文档
					groups.remove(group.getKey());
					docs.remove(group.getKey());
				}
			}
		}
	}
	
	private String bindDocument(Integer uid, Integer did) {
		logger.info("uid: " + uid + " did: " + did);
		remoteUserFromGroupIfExits(uid);
		if (groups.containsKey(did)) {
			groups.get(did).add(uid);
		} else {
			Set<Integer> uids = new HashSet<>();
			uids.add(uid);
			groups.put(did, uids);
		}

		if (docs.containsKey(did)) {
			return docs.get(did);
		}
		try {
			Document doc = documentRepository.findById(did).get();
			String content;
			if (doc.getLatestVersion() == null) {
				content = "{}";
			} else {
				content = versionRepository.findById(doc.getLatestVersion()).get().getContent();
			}
			docs.put(did, content);
			return content;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}
}