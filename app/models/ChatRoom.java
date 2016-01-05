package models;

import play.mvc.*;
import play.libs.*;
import play.libs.F.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import static akka.pattern.Patterns.ask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.util.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * A chat room is an Actor.
 */
public class ChatRoom extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    // Default room.
    static ActorRef defaultRoom = Akka.system().actorOf(Props.create(ChatRoom.class));
 
    // Members of this room.
    Map<String, ActorRef> members = new HashMap<String, ActorRef>();

    public void onReceive(Object message) throws Exception {

        if(message instanceof Join) {

            // Received a Join message
            Join join = (Join)message;
            System.out.println("onReceive(Join)-"+getSender());
              
            // Check if this username is free.
            if(members.containsKey(join.username)) {
                getSender().tell("This username is already used", self());
            } else {
                members.put(join.username, join.channel);
                System.out.println("=====> " + join.username + " : "+ join.channel);
                notifyAll("join", join.username, "has entered the room");
                getSender().tell("OK", self());       
            }

        } else if(message instanceof Talk)  {

            // Received a Talk message
            Talk talk = (Talk)message;
            System.out.println("onReceive(Talk)-"+getSender());
            
            notifyAll("talk", talk.username, talk.text);

        } else if(message instanceof Quit)  {
        	System.out.println("onReceive(Quit)-"+getSender());
        	Quit quit = (Quit)message;
        	System.out.println("quit.channel="+quit.channel);
        	System.out.println("members.get(quit.username)="+members.get(quit.username));
            if(quit.channel.equals(members.get(quit.username)))
            {
            	members.remove(quit.username);
                notifyAll("quit", quit.username, "has left the room");
            }
        } else {
            unhandled(message);
        }

    }

    // Send a Json event to all members
    public void notifyAll(String kind, String user, String text) {
        for(ActorRef channel: members.values()) {

            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("message", text);

            ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }
            System.out.println("notifyAll-"+getSender());
            //getSender().tell(event,getSelf());  
            channel.tell(event,null);
        }
    }

    // -- Messages

    public static class Join {

        final String username;
        final ActorRef channel;

        public Join(String username, ActorRef channel) {
            this.username = username;
            this.channel = channel;
        }

    }

    public static class Talk {

        final String username;
        final String text;

        public Talk(String username, String text) {
            this.username = username;
            this.text = text;
        }

    }

    public static class Quit {

        final String username;
        final ActorRef channel;

        public Quit(String username,ActorRef channel) {
            this.username = username;
            this.channel = channel;
        }

    }

}