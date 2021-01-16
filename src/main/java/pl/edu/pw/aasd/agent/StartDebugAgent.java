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

            cc.createNewAgent(
                    "Owner2",
                    OwnerAgent.class.getName(),
                    new Object[]{
                            "owner002"
                    }
            ).start();

            cc.createNewAgent(
                    "User1",
                    UserAgent.class.getName(),
                    new Object[]{
                            "user001"
                    }
            ).start();

            cc.createNewAgent(
                    "Partner1",
                    PartnerAgent.class.getName(),
                    new Object[]{
                            "partner001"
                    }
            ).start();


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
