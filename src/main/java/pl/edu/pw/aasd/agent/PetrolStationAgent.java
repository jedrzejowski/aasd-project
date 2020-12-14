package pl.edu.pw.aasd.agent;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.promise.Promise;

public class PetrolStationAgent extends Agent {

    StationDescription stationDescription;

    @Override
    protected void setup() {
        AgentHelper.registerServices(this, "petrolStation");

        AgentHelper.setupNewService(this, MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology("checkPrice")
        ), msg -> {
            ACLMessage reply = msg.createReply();
            reply.setOntology("priceReply");
            reply.setContent(getCurrentPrice().toJSON());
            this.send(reply);
        });

        AgentHelper.setupNewService(this, MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology("setStationDescription")
        ), msg -> {
            var stationDescription = StationDescription.from(msg.getContent());

            this.setStationDescription(msg.getSender(), stationDescription)
                    .thenAccept((__) -> {
                        ACLMessage reply = msg.createReply();
                        reply.setOntology("accepted");
                        reply.setContent("{}");
                        this.send(reply);
                    })
                    .onError(err -> {
                        ACLMessage reply = msg.createReply();
                        reply.setOntology("rejected");
                        reply.setContent("{}");
                        this.send(reply);
                    });
        });
    }

    public PetrolPrice getCurrentPrice() {
        var price = new PetrolPrice();
        price.setPb98((int) Math.floor(Math.random() * 10000) + "");
        price.setPb95((int) Math.floor(Math.random() * 10000) + "");
        return price;
    }

    public StationDescription getStationDescription() {
        return stationDescription;
    }

    public Promise<Void> setStationDescription(AID agent, StationDescription stationDescription) {
        return OwnerAgent.authOwner(agent)
                .thenApply((__) -> {
                    this.stationDescription = stationDescription;
                    return null;
                });
    }

    public static Promise<DFAgentDescription[]> findAll(Agent agent) {
        return AgentHelper.findAllOfService(agent, "petrolStation");
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
