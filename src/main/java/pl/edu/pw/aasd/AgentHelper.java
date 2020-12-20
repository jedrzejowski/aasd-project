package pl.edu.pw.aasd;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.promise.Promise;

public class AgentHelper {

    private static final Pattern serviceMatch = Pattern.compile("^([a-zA-Z]+):");

    public static void registerServices(Agent agent, String... serviceNames) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());


        for (var serviceName : serviceNames) {
            ServiceDescription sd = new ServiceDescription();

            if (serviceMatch.matcher(serviceName).find()) {
                var vals = serviceName.split(":");

                sd.setType(vals[0]);
                sd.setName(vals[1]);

                System.out.println(vals[1]);

            } else {
                sd.setType(serviceName);
                sd.setName(agent.getName());
            }

            dfd.addServices(sd);
        }

        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public static Promise<DFAgentDescription[]> findAllOf(Agent agent, String type, String name) {

        var template = new DFAgentDescription();

        var sd = new ServiceDescription();
        sd.setType(type);

        return new Promise<DFAgentDescription[]>().fulfillInAsync(() -> {
            return Arrays.stream(
                    DFService.search(agent, template)
            ).filter(description -> {
                if (name == null) {
                    return true;
                }

                var iterator = description.getAllServices();

                while (iterator.hasNext()) {
                    var desc = (ServiceDescription) iterator.next();
                    if (type.equals(desc.getType()) && name.equals(desc.getName())) {
                        return true;
                    }
                }

                return false;
            }).toArray(DFAgentDescription[]::new);
        });
    }

    public static Promise<DFAgentDescription[]> findAllOf(Agent agent, String type) {
        return findAllOf(agent, type, null);
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

        System.out.println(agent);

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

    public static <T extends Jsonable> Promise<T> oneShotMessage(
            Agent me,
            AID agent,
            String ontology,
            Jsonable obj,
            Class<T> cls
    ) {
        var gson = new Gson();

        var receiveMsgTemplate = MessageTemplate.and(
                MessageTemplate.MatchSender(agent),
                MessageTemplate.MatchOntology(ontology)
        );

        var message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology(ontology);
        message.addReceiver(agent);

        if (obj != null) {
            message.setContent(obj.toString());
            message.setLanguage("application/json");
        }

        return AgentHelper.oneShotMessage(me, message, receiveMsgTemplate)
                .thenApply(msg -> gson.fromJson(msg.getContent(), cls));
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
