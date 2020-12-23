package pl.edu.pw.aasd.agent;

import jade.core.Agent;

public class StartDebugAgent extends Agent {
    @Override
    protected void setup() {
        try {
            var cc = this.getContainerController();

            cc.createNewAgent(
                    "Owner1",
                    OwnerAgent.class.getName(),
                    new Object[]{
                            "owner001"
                    }
            ).start();


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
