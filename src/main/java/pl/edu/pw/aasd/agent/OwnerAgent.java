package pl.edu.pw.aasd.agent;

import com.google.gson.JsonParser;
import jade.core.AID;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Collection;


public class OwnerAgent extends AgentWithFace {

    String uniqueName = null;
    Collection<String> ownedPetrolStation = new ArrayList<>();

    @Override
    protected void setup() {
        this.uniqueName = this.getLocalName();

        AgentHelper.registerServices(this,
                "OwnerAgent",
                "OwnerAgent:" + this.getUniqueName()
        );
    }

    @Override
    protected void setupFace() {
        setupFaceOwnedPetrolStation();
    }

    public String getUniqueName() {
        return uniqueName;
    }

    //region ownedPetrolStation

    public Collection<String> getOwnedPetrolStation() {
        return ownedPetrolStation;
    }

    private void setupFaceOwnedPetrolStation() {

        this.faceHandle("/api/this/ownedPetrolStation", body -> {
            var owned = this.getOwnedPetrolStation();
            return Jsonable.toJson(owned);
        });

        this.faceHandle("/api/this/createPetrolStation", body -> {
            var request = body.getAsJsonObject();
            var uniqueName = request.get("uniqueName").getAsString();
            createPetrolStation(uniqueName);
            return null;
        });

    }

    //endregion

    private void createPetrolStation(String uniqueName) throws StaleProxyException {
        //TODO sprawdzić czy dana stacja istnieje

        var cc = this.getContainerController();

        cc.createNewAgent(
                uniqueName,
                PetrolStationAgent.class.getName(),
                new Object[]{this.uniqueName}
        ).start();

        ownedPetrolStation.add(uniqueName);
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
