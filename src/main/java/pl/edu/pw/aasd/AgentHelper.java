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
            sd.setName(serviceName);
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
            final Agent agent,
            final MessageTemplate template,
            final ServiceCallback callback
    ) {
        agent.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(template);

                if (msg != null) {
                    try {
                        callback.handle(msg);
                    } catch (Exception e) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.FAILURE);
                        agent.send(reply);
                    }
                } else {
                    block();
                }
            }
        });
    }

    public static void setupNewService(
            final Agent agent,
            final String template,
            final ServiceCallback callback
    ) {
        setupNewService(agent, MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchOntology(template)
        ), callback);
    }


    public interface ServiceCallback {
        void handle(ACLMessage msg);
    }


    static public void sendConfirm(Agent agent, ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.CONFIRM);
        agent.send(reply);
    }

    static public void sendFailure(Agent agent, ACLMessage msg, Throwable err) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.FAILURE);
        agent.send(reply);
    }

    static public <T extends Jsonable> void sendInform(Agent agent, ACLMessage msg, T obj) {
        var reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setLanguage("application/json");
        reply.setContent(obj.toString());
        agent.send(reply);
    }
}
