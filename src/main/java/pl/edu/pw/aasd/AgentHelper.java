package pl.edu.pw.aasd;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.concurrent.TimeoutException;

import pl.edu.pw.aasd.promise.Promise;

public class AgentHelper {

    public static void registerServices(Agent agent, String... serviceNames) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        for (var serviceName : serviceNames) {
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceName);
            dfd.addServices(sd);
        }

        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public static Promise<DFAgentDescription[]> findAllOfService(Agent agent, String serviceName) {

        var template = new DFAgentDescription();

        var sd = new ServiceDescription();
        sd.setType(serviceName);

        return new Promise<DFAgentDescription[]>()
                .fulfillInAsync(() -> DFService.search(agent, template));
    }

    public static Promise<ACLMessage> oneShotMessage(
            Agent agent,
            ACLMessage msg,
            MessageTemplate template
    ) {
        var promise = new Promise<ACLMessage>();

        var t = new Thread(() -> {
            try {
                Thread.sleep(10000);
                promise.fulfillExceptionally(new TimeoutException());
            } catch (Exception ignored) {
            }
        });
        t.start();

        agent.addBehaviour(new SimpleBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(template);

                if (msg != null) {
                    promise.fulfill(msg);

                } else {
                    block();
                }
            }

            @Override
            public boolean done() {
                return promise.isDone();
            }
        });

        agent.send(msg);

        return promise;
    }

    public static void setupNewService(
            Agent agent,
            final MessageTemplate template,
            ServiceCallback callback
    ) {
        agent.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(template);

                if (msg != null) {
                    callback.handle(msg);
                } else {
                    block();
                }
            }
        });
    }

    public interface ServiceCallback {
        void handle(ACLMessage msg);
    }
}
