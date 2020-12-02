package pl.edu.pw.aasd.agent;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.promise.Promise;

public class PetrolStationAgent extends Agent {

    @Override
    protected void setup() {

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("petrolStation");

        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        AgentHelper.setupNewService(this, MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology("checkPrice")
        ), msg -> {
            ACLMessage reply = msg.createReply();
            reply.setOntology("priceReply");
            reply.setContent(getCurrentPrice().toJSON());
            this.send(reply);
        });
    }

    public PetrolPrice getCurrentPrice() {
        var price = new PetrolPrice();
        price.setPb98((int) Math.floor(Math.random() * 10000) + "");
        price.setPb95((int) Math.floor(Math.random() * 10000) + "");
        return price;
    }

    public static Promise<DFAgentDescription[]> findAll(Agent agent) {

        var template = new DFAgentDescription();

        var sd = new ServiceDescription();
        sd.setType("petrolStation");

        return new Promise<DFAgentDescription[]>()
                .fulfillInAsync(() -> DFService.search(agent, template));
    }

    public static Promise<PetrolPrice> getCurrentPrice(Agent agent, AID petrol) {
        var gson = new Gson();

        var receiveMsgTemplate = MessageTemplate.and(
                MessageTemplate.MatchSender(petrol),
                MessageTemplate.MatchOntology("priceReply")
        );

        var message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology("checkPrice");
        message.addReceiver(petrol);

        return AgentHelper.oneShotMessage(agent, message, receiveMsgTemplate)
                .thenApply(msg -> gson.fromJson(msg.getContent(), PetrolPrice.class));
    }
}
