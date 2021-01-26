package pl.edu.pw.aasd.agent;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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

import java.util.concurrent.ExecutionException;

public class PylonAgent extends AgentWithFace<PylonAgent.MyData> {

    static class MyData extends Jsonable {
//        PetrolPrice petrolPrice;
    }

    @Override
    protected MyData parseData(String name) {
        return name == null ? new MyData() : Jsonable.from(name, MyData.class);
    }

    @Override
    protected void setup() {
        super.setup();

        this.handleHttpApi("/api/this/getPetrolPrice", body -> {
            return getPetrolPrice().get().toJson();
        });

        this.handleHttpApi("/api/this/setPetrolPrice", body -> {
            var petrolPrice = Jsonable.from(body, PetrolPrice.class);
            var station = getPetrolStationAgent().get();
            var success = PetrolStationAgent.setStationPrices(this, station, petrolPrice).get();
            return new JsonPrimitive(success);
        });
    }

    public Promise<AID> getPetrolStationAgent() {
        return PetrolStationAgent.findByUniqueName(this, this.getUniqueName())
                .thenApply(DFAgentDescription::getName);
    }

    public Promise<StationDescription> getPetrolStationDescription() {
        return new Promise<StationDescription>().fulfillInAsync(() -> {
            var aid = getPetrolStationAgent().get();
            return PetrolStationAgent.getStationDescription(this, aid).get();
        });
    }

    public Promise<PetrolPrice> getPetrolPrice() {
        return new Promise<PetrolPrice>().fulfillInAsync(() -> {
            var aid = getPetrolStationAgent().get();
            return PetrolStationAgent.getCurrentPetrolPrice(this, aid).get();
        });
    }

    public static Promise<DFAgentDescription> findByUniqueName(Agent agent, String uniqueName) {
        return AgentHelper.findOne(agent, "petrolStation:" + uniqueName);
    }

}
