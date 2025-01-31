package fr.becpg.artworks.annotation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/annotws/{store_type}/{store_id}/{id}")
public class AnnotationWSHandler {

	private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
	private static final Map<String, Map<String, String>> storedAnnotations = new ConcurrentHashMap<>();
	
	private static final Log logger = LogFactory.getLog(AnnotationWSHandler.class);

	/**
	 * <p>onOpen.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object.
	 * @param room a {@link java.lang.String} object.
	 * @param annot a {@link java.lang.String} object.
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("id") final String room) {
	    session.setMaxTextMessageBufferSize(10 * 1024 * 1024); // 10MB
	    session.setMaxBinaryMessageBufferSize(10 * 1024 * 1024); // 10MB

	    logger.debug("Connected ... " + session.getId() + " to room " + room);

	    session.getUserProperties().put("room", room);
	    AnnotationWSHandler.userSessions.put(session.getId(), session);

	    // Retrieve stored annotations for the room
	    Map<String, String> annotations = storedAnnotations.getOrDefault(room, new HashMap<>());

	    // Send existing annotations to the newly connected user
	    if (!annotations.isEmpty()) {
	        try {
	            JSONObject message = new JSONObject();
	            message.put("type", "SYNC");
	            message.put("annotations", annotations);

	            session.getBasicRemote().sendText(message.toString());

	        } catch (IOException e) {
	            logger.error("Failed to send stored annotations to new user", e);
	        }
	    }
	}

	/**
	 * <p>onMessage.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param session a {@link jakarta.websocket.Session} object.
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
	    logger.debug("Receiving ... " + session.getId());
	    String room = (String) session.getUserProperties().get("room");

	    JSONObject jsonMessage = new JSONObject(message);
	    
	    if (jsonMessage.has("type") && jsonMessage.has("id")) {
	        String annotationId = jsonMessage.getString("id");
	        String type = jsonMessage.getString("type");
	        
	        // Only ADD and MODIFY contain XFDF data
	        String xfdf = jsonMessage.optString("xfdf", null);

	        if ("DELETE".equals(type)) {
	            storedAnnotations.computeIfAbsent(room, k -> new HashMap<>()).remove(annotationId);
	        } else if ("ADD".equals(type) || "MODIFY".equals(type)) {
	            if (xfdf != null) {
	                storedAnnotations.computeIfAbsent(room, k -> new HashMap<>()).put(annotationId, xfdf);
	            }
	        }

	        // Broadcast to all other users in the same room
	        try {
	            for (Session s : AnnotationWSHandler.userSessions.values()) {
	                if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
	                    s.getBasicRemote().sendText(message);
	                }
	            }
	        } catch (IOException e) {
	            logger.warn("Error broadcasting message: " + e.getMessage());
	            logger.debug("onMessage failed", e);
	        }
	    }
	}


	/**
	 * <p>onClose.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object.
	 * @param closeReason a {@link jakarta.websocket.CloseReason} object.
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.debug(String.format("Session %s closed because of %s", session.getId(), closeReason));
		AnnotationWSHandler.userSessions.remove(session.getId());
		String room = (String) session.getUserProperties().get("room");
		if (!isRoomStillInUse(room)) {
			storedAnnotations.remove(room);
		}
	}
	
	private boolean isRoomStillInUse(String room) {
		for (Session s : AnnotationWSHandler.userSessions.values()) {
			if (s.isOpen() && room.equals(s.getUserProperties().get("room"))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>onError.</p>
	 *
	 * @param session a {@link jakarta.websocket.Session} object.
	 * @param thr a {@link java.lang.Throwable} object.
	 */
	@OnError
	public void onError(Session session, Throwable thr) {
		if (!session.isOpen()) {
			String room = (String) session.getUserProperties().get("room");
			AnnotationWSHandler.userSessions.remove(session.getId());
			if (!isRoomStillInUse(room)) {
				storedAnnotations.remove(room);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug(thr, thr);
		}
	}

}
