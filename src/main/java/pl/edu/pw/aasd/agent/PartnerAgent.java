package pl.edu.pw.aasd.agent;

import com.google.gson.JsonObject;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PartnerPromotion;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PartnerAgent extends AgentWithFace<PartnerAgent.MyData> {

    static class MyData extends Jsonable {
        HashMap<String, PartnerPromotion> partnerPromotions = new HashMap<>();
    }

    @Override
    protected MyData parseData(String name) {
        return name == null ? new MyData() : Jsonable.from(name, MyData.class);
    }

    @Override
    protected void setup() {
        super.setup();
        AgentHelper.registerServices(this, "PartnerAgent:" + this.getUniqueName());

        AgentHelper.setupRequestResponder(this,
                ACLMessage.QUERY_REF, "getPromotions",
                msg -> Promise.fulfilled(Jsonable.toJson(this.data.partnerPromotions.values()))
        );

        this.handleHttpApi("/api/this/createPromotion", body -> {
            var request = body.getAsJsonObject();
            var promotion = Jsonable.from(request, PartnerPromotion.class);
            addPromotion(promotion);
            return new JsonObject();
        });

        this.handleHttpApi("/api/this/getPromotions",
                body -> Jsonable.toJson(getPromotions()));


//        AgentHelper.setupRequestResponder(this,
//                ACLMessage.REQUEST, "createPromotion",
//                msg -> {
//                    var promotion = PartnerPromotion.from(msg.getContent());
//                    this.addPromotion(promotion);
//
//                    var reply = msg.createReply();
//                    reply.setPerformative(ACLMessage.CONFIRM);
//                    this.send(reply);
//                }
//        );
//
//        AgentHelper.setupRequestResponder(this,
//                ACLMessage.QUERY_REF, "getPromotions", msg -> {
//                    var reply = msg.createReply();
//                    reply.setPerformative(ACLMessage.INFORM);
//                    reply.setLanguage("application/json");
//                    reply.setContent(Jsonable.toString(partnerPromotions.values()));
//                    this.send(reply);
//                });
    }

    private void addPromotion(PartnerPromotion promotion) {
        this.data.partnerPromotions.put(promotion.getId(), promotion);
    }

    public Collection<PartnerPromotion> getPromotions(){
        return this.data.partnerPromotions.values();
    }

    static public DFAgentDescription[] findAll(Agent agent){
        return AgentHelper.findAllOf(agent, "PartnerAgent");
    }

    static public Promise<Collection<PartnerPromotion>> getPromotions(Agent me, AID partner){
        return AgentHelper.requestInteraction(
                me, partner,
                ACLMessage.QUERY_REF, "getPromotions",
                null
        ).thenApply(jsonElement -> Jsonable.fromList(jsonElement, PartnerPromotion.class));
    }
}
