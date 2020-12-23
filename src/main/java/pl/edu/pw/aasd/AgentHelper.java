package pl.edu.pw.aasd;

import com.google.gson.JsonElement;
import jade.core.AID;
import jade.core.Agent;
import jade.core.NotFoundException;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.net.ConnectException;
import java.util.Date;

import java.util.HashMap;
import java.util.regex.Pattern;

import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import pl.edu.pw.aasd.promise.Promise;

public class AgentHelper {

    private static final Pattern serviceMatch = Pattern.compile("^([a-zA-Z]+):");

    public static void registerServices(Agent agent, String... serviceNames) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        for (var serviceName : serviceNames) {
            dfd.addServices(stringToServiceDescription(serviceName));
        }

        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    public static ServiceDescription stringToServiceDescription(String str) {
        var sd = new ServiceDescription();

        if (serviceMatch.matcher(str).find()) {
            var vals = str.split(":");

            sd.setType(vals[0]);
            sd.setName(vals[1]);

        } else {
            sd.setType(str);
            sd.setName(str);
        }

        return sd;
    }

    public static DFAgentDescription[] findAllOf(Agent agent, String... serviceNames) {

        DFAgentDescription template = new DFAgentDescription();
        for (var serviceName : serviceNames) {
            template.addServices(stringToServiceDescription(serviceName));
        }

        try {
            return DFService.search(agent, template);
        } catch (Throwable e) {
            return new DFAgentDescription[0];
        }
    }

    public static Promise<DFAgentDescription> findOne(Agent agent, String... serviceNames) {
        return new Promise<DFAgentDescription>().fulfillInAsync(() -> {
            var desc = findAllOf(agent, serviceNames);
            if (desc.length == 1) {
                return desc[0];
            } else {
                throw new NotFoundException();
            }
        });
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

    public static Promise<JsonElement> requestInteraction(
            Agent me, AID agent,
            int performative, String ontology,
            JsonElement content
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
                .thenApply(response -> Jsonable.parseString(response.getContent()));
    }


    public interface ResponderI {
        Promise<JsonElement> handle(ACLMessage msg) throws Throwable;
    }

    public static void setupRequestResponder(
            final Agent agent,
            final MessageTemplate template,
            final ResponderI callback
    ) {
        var aclMap = new HashMap<ACLMessage, Promise<JsonElement>>();

        agent.addBehaviour(new AchieveREResponder(agent, template) {
            protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {

                try {
                    var promise = callback.handle(request);
                    if (promise == null) {
                        throw new RefuseException("");
                    }

                    aclMap.put(request, promise);

                    ACLMessage agree = request.createReply();
                    agree.setPerformative(ACLMessage.AGREE);
                    return agree;

                } catch (RefuseException | NotUnderstoodException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RefuseException("");
                }
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {

                try {
                    var promise = aclMap.remove(request);
                    var obj = promise.get();

                    ACLMessage inform = request.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    inform.setLanguage("application/json");
                    inform.setContent(obj.toString());
                    return inform;

                } catch (Throwable e) {
                    throw new FailureException("");
                }
            }
        });
    }

    public static void setupRequestResponder(
            final Agent agent,
            final int performative,
            final String ontology,
            final ResponderI callback
    ) {
        var template = MessageTemplate.and(
                MessageTemplate.MatchPerformative(performative),
                MessageTemplate.MatchOntology(ontology)
        );

        setupRequestResponder(agent, template, callback);
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

}
