package pl.edu.pw.aasd.agent;

import jade.core.AID;
import jade.core.Agent;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.promise.Promise;


public class OwnerAgent extends AgentWithFace {

    String uniqueName = null;
    String[] ownedPetrolStation = null;

    @Override
    protected void setup() {
        this.uniqueName = this.getLocalName();

        AgentHelper.registerServices(this, "OwnerAgent:" + this.getUniqueName());
    }

    @Override
    protected void setupFace() {

        this.faceHandle("/api/this/getOwnedPetrolStation", body -> {
            var owned = this.getOwnedPetrolStation();
            var str = Jsonable.toJson(owned);
            return Promise.fulfilled(str);
        });
    }

    public String getUniqueName() {
        return uniqueName;
    }

    //region ownedPetrolStation

    public String[] getOwnedPetrolStation() {
        return ownedPetrolStation;
    }

    public void setOwnedPetrolStation(String[] ownedPetrolStation) {
        this.ownedPetrolStation = ownedPetrolStation;
    }

    //endregion

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
