package pl.edu.pw.aasd.agent;

import jade.core.AID;
import jade.core.Agent;
import pl.edu.pw.aasd.promise.Promise;


public class OwnerAgent extends Agent {


    @Override
    protected void setup() {

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
