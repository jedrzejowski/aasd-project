package pl.edu.pw.aasd.agent;

import com.google.gson.JsonElement;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.promise.Promise;

public class PylonAgent extends AgentWithFace<PylonAgent.MyData> {

    static class MyData extends Jsonable {
        PetrolPrice price;
    }

    @Override
    protected MyData parseData(String name) {
        return name == null ? new MyData() : Jsonable.from(name, MyData.class);
    }

    @Override
    protected void setup() {


        // Aktualizacja ceny
        AgentHelper.setupRequestResponder(this,
                ACLMessage.REQUEST, "setPrice",
                msg -> new Promise<JsonElement>().fulfillInAsync(() -> {
                    OwnerAgent.authOwner(msg.getSender()).get();
                    return null;
                })
        );
    }

    public Promise<AID> getPetrolStationAgent() {
        return PetrolStationAgent.findByUniqueName(this, this.getUniqueName())
                .thenApply(DFAgentDescription::getName);
    }

    public Promise<StationDescription> getPetrolStationDescription() {
        var promise = new Promise<StationDescription>();
        promise.fulfillInAsync(() -> {
            var aid = getPetrolStationAgent().get();
            return PetrolStationAgent.getStationDescription(this, aid).get();
        });
        return promise;
    }

    public static Promise<DFAgentDescription> findByUniqueName(Agent agent, String uniqueName) {
        return AgentHelper.findOne(agent, "petrolStation:" + uniqueName);
    }

}
