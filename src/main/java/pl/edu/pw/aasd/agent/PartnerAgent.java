package pl.edu.pw.aasd.agent;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PartnerPromotion;

import java.util.HashMap;

public class PartnerAgent extends Agent {

    HashMap<String, PartnerPromotion> partnerPromotions = new HashMap<>();

    @Override
    protected void setup() {
        AgentHelper.registerServices(this, "partner");

        AgentHelper.setupNewService(this, "createPromotion", msg -> {
            var promotion = PartnerPromotion.from(msg.getContent());
            this.addPromotion(promotion);

            var reply = msg.createReply();
            reply.setPerformative(ACLMessage.CONFIRM);
            this.send(reply);
        });

        AgentHelper.setupNewService(this, "getPromotions", msg -> {
            var reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setLanguage("application/json");
            reply.setContent(Jsonable.toJson(partnerPromotions.values()));
            this.send(reply);
        });
    }

    private void addPromotion(PartnerPromotion promotion) {
        partnerPromotions.put(promotion.getId(), promotion);

    }
}
