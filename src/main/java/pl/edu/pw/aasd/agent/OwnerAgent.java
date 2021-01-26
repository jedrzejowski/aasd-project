package pl.edu.pw.aasd.agent;

import com.google.gson.JsonObject;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import pl.edu.pw.aasd.*;
import pl.edu.pw.aasd.data.Near;
import pl.edu.pw.aasd.data.RadiusRequest;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class OwnerAgent extends AgentWithFace<OwnerAgent.MyData> {

    static class MyData extends Jsonable {
        Collection<String> ownedPetrolStation = new ArrayList<>();
    }

    @Override
    protected MyData parseData(String name) {
        return name == null ? new MyData() : Jsonable.from(name, MyData.class);
    }

    @Override
    protected void setup() {
        super.setup();

        AgentHelper.registerServices(this,
                "OwnerAgent:" + this.getUniqueName()
        );

        this.handleHttpApi("/api/this/ownedPetrolStation",
                body -> Jsonable.toJson(this.data.ownedPetrolStation));

        this.handleHttpApi("/api/this/createPetrolStation", body -> {
            var request = body.getAsJsonObject();
            var uniqueName = request.get("uniqueName").getAsString();
            createPetrolStation(uniqueName).get();
            return new JsonObject();
        });

        this.handleHttpApi("/api/this/findNearPetrolStation", body -> {
            var request = body.getAsJsonObject();

            var descriptions = PetrolStationAgent.findAll(this);

            var names = Arrays.stream(descriptions)
                    .map(DFAgentDescription::getName)
                    .map(aid -> {
                        var obj = new JsonObject();
                        obj.addProperty("name", aid.getName());

                        try {
                            var uniqueNamePromise = AgentWithUniqueName.getUniqueName(this, aid);
                            var stationDescriptionPromise = PetrolStationAgent.getStationDescription(this, aid);
                            var petrolPricePromise = PetrolStationAgent.getCurrentPetrolPrice(this, aid);

                            if (this.data.ownedPetrolStation.contains(uniqueNamePromise.get())) {
                                return null;
                            }

                            obj.addProperty("uniqueName", uniqueNamePromise.get());
                            obj.add("stationDescription", stationDescriptionPromise.get().toJson());
                            obj.add("petrolPrice", petrolPricePromise.get().toJson());
                        } catch (Throwable ignore) {
                            return null;
                        }

                        return obj;
                    })
                    .filter(Objects::nonNull)
                    .toArray();

            return Jsonable.toJson(names);
        });

        if (Boot.DEBUG_CREATE_CHILDREN) {
            this.data.ownedPetrolStation.forEach(this::startPetrolStation);
        }
    }

    private Promise<Void> createPetrolStation(String uniqueName) {
        return new Promise<Void>().fulfillInAsync(() -> {
            //TODO sprawdzić czy dana stacja istnieje

            var cc = this.getContainerController();

            var ac = cc.createNewAgent(
                    uniqueName,
                    PetrolStationAgent.class.getName(),
                    new Object[]{uniqueName}
            );
            ac.start();

            data.ownedPetrolStation.add(uniqueName);
            return null;
        });
    }

    private Promise<Void> startPetrolStation(String uniqueName) {
        return new Promise<Void>().fulfillInAsync(() -> {
            //TODO sprawdzić czy dana stacja istnieje

            var cc = this.getContainerController();

            var ac = cc.createNewAgent(
                    uniqueName,
                    PetrolStationAgent.class.getName(),
                    new Object[]{uniqueName}
            );
            ac.start();
            return null;
        });
    }

    public static Promise<Void> authOwner(final AID maybe_owner) {
        var promise = new Promise<Void>();

        var t = new Thread(() -> {
            try {
                Thread.sleep(500);
                promise.fulfill(null);
            } catch (Exception ignored) {
            }
        });
        t.start();

        return promise;
    }

    private static double countSquareDistance(double lat1, double lat2, double long1, double long2) {
        var temp1 = (lat1 - lat2);
        var temp2 = (long1 - long2);
        return temp1 * temp1 + temp2 * temp2;
    }
}
