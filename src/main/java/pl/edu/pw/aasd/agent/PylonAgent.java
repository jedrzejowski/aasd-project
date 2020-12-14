package pl.edu.pw.aasd.agent;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.data.PetrolPrice;

public class PylonAgent extends Agent {

    PetrolPrice price;

    @Override
    protected void setup() {

        // Aktualizacja ceny
        AgentHelper.setupNewService(this, MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology("setPrice")
        ), msg -> {
            OwnerAgent.authOwner(msg.getSender())
                    .thenAccept((a) -> {
                        ACLMessage reply = msg.createReply();
                        reply.setOntology("answer");
                        reply.setContent("ok");
                        this.send(reply);
                    });
        });
    }

    public PetrolPrice getPrice() {
        return price;
    }

    private void setPrice(PetrolPrice price) {
        this.price = price;
    }
}
