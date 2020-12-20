package pl.edu.pw.aasd.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.NotFoundException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.promise.Promise;

public class PylonAgent extends AgentWithFace {

    PetrolPrice price;
    String stationUniqueName;

    @Override
    protected void setup() {

        var args = this.getArguments();
        if (args.length != 1 || !(args[0] instanceof String)) {
            System.err.println("Brak nazwy agenta");
            throw new RuntimeException();
        }
        this.stationUniqueName = (String) args[0];

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

    public String getStationUniqueName() {
        return this.stationUniqueName;
    }

    public Promise<AID> getPetrolStationAgent() {
        return new Promise<AID>().fulfillInAsync(() -> {
            var dfAgentDescriptions = PetrolStationAgent.findByUniqueName(this, this.getStationUniqueName()).get();

            if (dfAgentDescriptions.length != 1) {
                throw new RuntimeException();
            }

            return dfAgentDescriptions[0].getName();
        });
    }

    public Promise<StationDescription> getPetrolStationDescription() {
        var promise = new Promise<StationDescription>();
        promise.fulfillInAsync(() -> {
            var aid = getPetrolStationAgent().get();
            return PetrolStationAgent.getStationDescription(this, aid).get();
        });
        return promise;
    }

    @Override
    protected void setupFace() {
        this.faceHandle("/api/pylon/stationUniqueName", body -> Promise.fulfilled(this.getStationUniqueName()));

        this.faceHandle("/api/pylon/stationDescription", body -> this.getPetrolStationDescription().thenApply(Jsonable::toString));

    }

}
