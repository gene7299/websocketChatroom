#websocketChatroom
A simple chatroom example implemented by unsing Akka Actor in Play Framework 2.x (Java)

Please see AkkaActor_Presentation.pdf & WebsocketIntrox.pdf to learn more.
http://www.slideshare.net/gene7299/akka-actor-presentation 
http://www.slideshare.net/gene7299/websocketintrox

0.Define

	import akka.actor.UntypedActor;
	import akka.event.Logging;
	import akka.event.LoggingAdapter;
	import akka.japi.Procedure;
	
	public class AnActor extends UntypedActor {
	    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		
		public void onReceive(Object message){
	        if (message instanceof String) {
	            log.info((String) message);              
	        }else{
	    	 unhandled(message);
	    	 log.info("Unhandled message");
	        }
		}
	}


1.Create

	package controllers;
	import akka.actor.ActorRef;
	import akka.actor.Props;
	import play.libs.Akka;
	import play.mvc.*;
	public class HelloActor extends Controller {
	    
	    public static Result index() {
	    	 ActorRef actor = Akka.system().actorOf(Props.create(AnActor.class));
			 // insert stuff actor.tell(message)
	    	 return ok("ok");
	    }
	}


2.Send

	package controllers;
	import akka.actor.ActorRef;
	import akka.actor.Props;
	import play.libs.Akka;
	import play.mvc.*;
	public class HelloActor extends Controller {
	    
	    public static Result index() {
	    	 ActorRef actor = Akka.system().actorOf(Props.create(AnActor.class));
		 actor.tell("Hello Actor!!", null);
	    	 return ok("ok");
	    }
	}


3.Become

	getContext().become(Procedure<Object>);
