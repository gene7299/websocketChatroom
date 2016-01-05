package models;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import play.libs.Json;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.ChatRoom.*;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class ChatWebSocketActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
    public static Props props(ActorRef out, String username) {
        return Props.create(ChatWebSocketActor.class, out, username);
    }

    private final ActorRef out;
    private final String username;

    public ChatWebSocketActor(ActorRef out,String username) {
    	log.info("out="+out);
        this.out = out;
        this.username = username;
    }
    public void preStart()
    {
    	 try { 
    		 System.out.println("=================================================");
	    	final Timeout timeout = new Timeout(Duration.create(1, SECONDS));
	    	Future<Object> rt = Patterns.ask(models.ChatRoom.defaultRoom,new Join(username, out), timeout);
	    	String result = (String)Await.result(rt, timeout.duration());
        	System.out.println("-O-"+getSender());
        	System.out.println("-O-"+getSelf());
        	System.out.println("-O-"+getContext().self());
        	System.out.println("-O-"+getContext().parent());
	    	if("OK".equals(result)) {
	    		System.out.println("OK!");
	    		//go!
	    	}else {
	            // Cannot connect, create a Json error.
	          ObjectNode error = Json.newObject();
	          error.put("error", result);
	            // Send the error to the socket.
	          out.tell(error,null);
	          System.out.println("ERROR!");
	           // getContext().parent().tell(PoisonPill.getInstance(), self());
	    		out.tell(PoisonPill.getInstance(), self());
	        }
         } catch (Exception ex) {
             ex.printStackTrace();
            //force to stop actor
         }
    }
    
    public void onReceive(Object message) throws Exception {
    	
    	System.out.println("----------------------------------------------------------");
    	if(getSender().equals(getContext().parent()))
    	{
        	System.out.println("-A-"+getSender()); // == getContext().parent()
        	System.out.println("-A-"+getSelf());
        	System.out.println("-A-"+getContext().self());
        	System.out.println("-A-"+getContext().parent());
    	   	if (message instanceof JsonNode)
        	{
    	   		System.out.println("-A-"+"GO!");
        		JsonNode event = (JsonNode)message;
        		models.ChatRoom.defaultRoom.tell(new Talk(username, event.get("text").asText()), getSender());
            } else {
                unhandled(message);
            }
    	}else
    	{
        	System.out.println("-B-"+getSender());
        	System.out.println("-B-"+getSelf());
        	System.out.println("-B-"+getContext().self());
        	System.out.println("-B-"+getContext().parent());
    	   	if (message instanceof ObjectNode)
        	{
    	   		System.out.println("-B-"+"GO!");
        		//JsonNode event = (JsonNode)message;
        		out.tell(message, ActorRef.noSender());
            } else {
                unhandled(message);
            }
    	}
 
    }
    public void postStop() throws Exception {
    	System.out.println("Disconnected");
    	System.out.println("-C-"+getSender());
    	System.out.println("-C-"+getSelf());
    	System.out.println("-C-"+getContext().self());
    	System.out.println("-C-"+getContext().parent());
    	models.ChatRoom.defaultRoom.tell(new Quit(username,getContext().parent()), null);
    }
}