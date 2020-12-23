package pl.edu.pw.aasd.agent;

import com.google.gson.JsonObject;
import jade.core.AID;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Boot;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Collection;


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

}
