package pl.edu.pw.aasd.agent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Boot;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.data.UserVote;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Collection;

public class PetrolStationAgent extends AgentWithFace<PetrolStationAgent.MyData> {

    static class MyData extends Jsonable {
        StationDescription stationDescription = null;
        PetrolPrice currentPetrolPrice = null;
        Collection<UserVote> votes = new ArrayList<>();
    }

    @Override
    protected MyData parseData(String name) {
        return name == null ? new MyData() : Jsonable.from(name, MyData.class);
    }

    @Override
    protected void setup() {
        super.setup();

        AgentHelper.registerServices(this,
                "petrolStation:" + this.getUniqueName()
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.QUERY_REF, "currentPetrolPrice",
                msg -> Promise.fulfilled(this.data.currentPetrolPrice.toJson())
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.QUERY_REF, "stationDescription",
                msg -> Promise.fulfilled(this.data.stationDescription.toJson())
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.REQUEST, "stationDescription",
                msg -> new Promise<JsonElement>().fulfillInAsync(() -> {
                    var stationDescription = StationDescription.from(msg.getContent());
                    OwnerAgent.authOwner(msg.getSender()).get();
                    this.data.stationDescription = stationDescription;
                    return new JsonPrimitive(true);
                })
        );

        if (Boot.DEBUG_CREATE_CHILDREN) {
            this.createPylon();
        }
    }

    public static Promise<PetrolPrice> getCurrentPetrolPrice(Agent agent, AID petrol) {
        return AgentHelper.requestInteraction(
                agent, petrol,
                ACLMessage.QUERY_REF, "currentPetrolPrice",
                null, PetrolPrice.class
        );
    }

    public static Promise<StationDescription> getStationDescription(Agent me, AID station) {
        return AgentHelper.requestInteraction(
                me, station,
                ACLMessage.QUERY_REF, "getStationDescription",
                null, StationDescription.class);
    }


    public static Promise<DFAgentDescription[]> findAll(Agent agent) {
        return AgentHelper.findAllOf(agent, "petrolStation");
    }

    public static Promise<DFAgentDescription> findByUniqueName(Agent agent, String uniqueName) {
        return AgentHelper.findOne(agent, "petrolStation:" + uniqueName);
    }

    private Promise<Void> createPylon() {
        return new Promise<Void>().fulfillInAsync(() -> {

            var cc = this.getContainerController();

            var pylonAgent = cc.createNewAgent(
                    "PylonOf" + this.getLocalName(),
                    "pl.edu.pw.aasd.agent.PylonAgent",
                    new Object[]{
                            this.getUniqueName()
                    }
            );

            pylonAgent.start();
            return null;
        });
    }
}
