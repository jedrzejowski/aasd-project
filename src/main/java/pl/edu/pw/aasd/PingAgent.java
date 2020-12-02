package pl.edu.pw.aasd;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PingAgent extends Agent {
    private MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
            MessageTemplate.MatchOntology("presence")
    );

    protected void setup() {

        addBehaviour(new CyclicBehaviour(this) {

            public void action() {

                ACLMessage msg = myAgent.receive(template);
                if (msg != null) {

                    System.out.println("Received QUERY_IF message from agent " + msg.getSender().getName());

                    ACLMessage reply = msg.createReply();

                    if ("alive".equals(msg.getContent())) {
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("alive");
                    } else {
                        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                        reply.setContent("Unknown-content");
                    }

                    myAgent.send(reply);

                } else {
                    block();
                }
            }

        });

    }
}
