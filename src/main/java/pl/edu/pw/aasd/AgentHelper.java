package pl.edu.pw.aasd;

import jade.core.AID;
import jade.core.Agent;
import jade.core.NotFoundException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.net.ConnectException;
import java.util.Date;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import jade.proto.AchieveREInitiator;
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

    public static Promise<ACLMessage> requestInteraction(
            Agent me,
            ACLMessage msg
    ) {
        var promise = new Promise<ACLMessage>();

        me.addBehaviour(new AchieveREInitiator(me, msg) {
            protected void handleInform(ACLMessage inform) {
                promise.fulfill(inform);
            }

            protected void handleRefuse(ACLMessage refuse) {
                promise.fulfillExceptionally(new RuntimeException());
            }

            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    promise.fulfillExceptionally(new ConnectException());
                } else {
                    promise.fulfillExceptionally(new NotFoundException());
                }
            }
        });

        return promise;
    }

    public static <T extends Jsonable> Promise<T> requestInteraction(
            Agent me,
            AID agent,
            int performative,
            String ontology,
            Jsonable content,
            Class<T> cls
    ) {
        var msg = new ACLMessage(performative);
        msg.setOntology(ontology);
        msg.addReceiver(agent);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

        if (content != null) {
            msg.setContent(content.toString());
            msg.setLanguage("application/json");
        }

        return requestInteraction(me, msg)
                .thenApply(response -> Jsonable.from(response.getContent(), cls));
    }

    public static void setupRequestResponder(
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

    public static void setupRequestResponder(
            final Agent agent,
            final int performative,
            final String ontology,
            final ServiceCallback callback
    ) {
        var template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(performative),
                MessageTemplate.MatchOntology(ontology)
        );

        setupRequestResponder(agent, template, callback);
    }


    public interface ServiceCallback {
        void handle(ACLMessage msg);
    }

    static public <T extends Jsonable> void reply(Agent agent, ACLMessage msg, int performative, T obj) {
        var reply = msg.createReply();
        reply.setPerformative(performative);

        if (obj != null) {
            reply.setLanguage("application/json");
            reply.setContent(obj.toString());
        }

        agent.send(reply);
    }

    static public <T extends Jsonable> void replyInform(Agent agent, ACLMessage msg, T obj) {
        reply(agent, msg, ACLMessage.INFORM, obj);
    }

    static public <T extends Jsonable> void replyConfirm(Agent agent, ACLMessage msg, T obj) {
        reply(agent, msg, ACLMessage.CONFIRM, obj);
    }

    static public void replyFailure(Agent agent, ACLMessage msg, Throwable obj) {
        reply(agent, msg, ACLMessage.FAILURE, null);
    }

}
