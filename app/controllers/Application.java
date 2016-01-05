package controllers;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.fasterxml.jackson.databind.JsonNode;

import models.ChatRoom;
import models.ChatWebSocketActor;
import play.*;
import play.libs.F.Function;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }
    /**
     * Display the chat room.
     */
    public static Result chatRoom(String username) {
        if(username == null || username.trim().equals("")) {
            flash("error", "Please choose a valid username.");
            return redirect(routes.Application.index());
        }
        return ok(chatRoom.render(username));
    }

    public static Result chatRoomJs(String username) {
        return ok(views.js.chatRoom.render(username));
    }
  
    
	public static WebSocket<JsonNode> chat(final String username)  {
		return WebSocket.withActor(new Function<ActorRef, Props>() {
	        public Props apply(ActorRef out) throws Throwable {
	            return ChatWebSocketActor.props(out, username);
	        }
	    });
	}
	
}
