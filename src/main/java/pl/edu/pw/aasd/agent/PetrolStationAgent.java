package pl.edu.pw.aasd.agent;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.data.UserVote;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Collection;

public class PetrolStationAgent extends AgentWithFace {

    String uniqueName = null;
    StationDescription stationDescription = null;
    Collection<UserVote> votes = new ArrayList<>();

    @Override
    protected void setup() {
        //TODO lepiej to zrobić
        this.uniqueName = getLocalName();

        var sd = new StationDescription();
        sd.setCommonName("KK");
        this.setStationDescription(sd);

        AgentHelper.registerServices(this, "petrolStation:" + uniqueName);

        AgentHelper.setupNewService(this, "checkPrice", msg -> {
            AgentHelper.sendInform(this, msg, getCurrentPrice());
        });

        AgentHelper.setupNewService(this, "getStationDescription", msg -> {
            AgentHelper.sendInform(this, msg, this.getStationDescription());
        });

        AgentHelper.setupNewService(this, "setStationDescription", msg -> {
            var stationDescription = StationDescription.from(msg.getContent());

            OwnerAgent.authOwner(msg.getSender())
                    .thenAccept((__) -> this.setStationDescription(stationDescription))
                    .thenAccept((__) -> AgentHelper.sendConfirm(this, msg))
                    .onError(err -> AgentHelper.sendFailure(this, msg, err));
        });

        AgentHelper.setupNewService(this, "setPrice", msg -> {

        });

        AgentHelper.setupNewService(this, "addPriceProposition", msg -> {

        });

        AgentHelper.setupNewService(this, "addVote", msg -> {

        });


        this.createPylon();
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public PetrolPrice getCurrentPrice() {
        var price = new PetrolPrice();
        price.setPb98((int) Math.floor(Math.random() * 10000) + "");
        price.setPb95((int) Math.floor(Math.random() * 10000) + "");
        return price;
    }

    //region stationDescription

    public StationDescription getStationDescription() {
        return stationDescription;
    }

    public void setStationDescription(StationDescription stationDescription) {
        this.stationDescription = stationDescription;
    }

    public static Promise<StationDescription> getStationDescription(Agent me, AID station) {
        return AgentHelper.oneShotMessage(me, station, "getStationDescription", null, StationDescription.class);
    }

    //endregion

    public static Promise<DFAgentDescription[]> findAll(Agent agent) {
        return AgentHelper.findAllOf(agent, "petrolStation");
    }

    public static Promise<DFAgentDescription[]> findByUniqueName(Agent agent, String uniqueName) {
        return AgentHelper.findAllOf(agent, "petrolStation", uniqueName);
    }

    public static Promise<PetrolPrice> getCurrentPrice(Agent agent, AID petrol) {
        var gson = new Gson();

        var receiveMsgTemplate = MessageTemplate.and(
                MessageTemplate.MatchSender(petrol),
                MessageTemplate.MatchOntology("priceReply")
        );

        var message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology("checkPrice");
        message.addReceiver(petrol);

        return AgentHelper.oneShotMessage(agent, message, receiveMsgTemplate)
                .thenApply(msg -> gson.fromJson(msg.getContent(), PetrolPrice.class));
    }


    /**
     * Każda stacja tworzy agenta dla pylonu
     * Wiem, że to z punktu działania systemu jest głupie, no ale nia mam lepszego pomysłu
     */
    private void createPylon() {
        //TODO dodać sprawdzanie czy istnieje już agent pylona
        try {
            var cc = this.getContainerController();
            Object[] args = {this.uniqueName};

            var pylonAgent = cc.createNewAgent(
                    "PylonOf" + this.getLocalName(),
                    "pl.edu.pw.aasd.agent.PylonAgent",
                    args
            );

            pylonAgent.start();
            //TODO dodać stop
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setupFace() {

    }
}
