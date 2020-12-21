package pl.edu.pw.aasd.agent;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.data.UserVote;
import pl.edu.pw.aasd.promise.Promise;

import java.util.ArrayList;
import java.util.Collection;

public class PetrolStationAgent extends Agent {

    String uniqueName = null;
    String uniqueNameOfOwner = null;
    StationDescription stationDescription = null;
    PetrolPrice currentPetrolPrice = null;
    Collection<UserVote> votes = new ArrayList<>();

    @Override
    protected void setup() {
        this.uniqueName = getLocalName();
        this.uniqueNameOfOwner = this.getArguments()[0].toString();


        AgentHelper.registerServices(this,
                "petrolStation",
                "petrolStation:" + uniqueName
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.QUERY_REF, "currentPetrolPrice",
                msg -> AgentHelper.reply(this, msg, ACLMessage.INFORM, this.getCurrentPetrolPrice())
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.QUERY_REF, "stationDescription",
                msg -> AgentHelper.replyInform(this, msg, this.getStationDescription())
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.REQUEST,
                "setStationDescription", msg -> {
                    var stationDescription = StationDescription.from(msg.getContent());

                    OwnerAgent.authOwner(msg.getSender())
                            .thenAccept((__) -> this.setStationDescription(stationDescription))
                            .thenAccept((__) -> AgentHelper.replyConfirm(this, msg, null))
                            .onError(err -> AgentHelper.replyFailure(this, msg, err));
                }
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.PROPAGATE, "setPrice",
                msg -> {

                }
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.PROPAGATE, "addPriceProposition",
                msg -> {

                }
        );

        AgentHelper.setupRequestResponder(this,
                ACLMessage.PROPAGATE, "addVote", msg -> {

                }
        );


        this.createPylon();
    }

    public String getUniqueName() {
        return uniqueName;
    }

    //region currentPetrolPrice

    public PetrolPrice getCurrentPetrolPrice() {
        return currentPetrolPrice;
    }

    private void setCurrentPetrolPrice(PetrolPrice currentPetrolPrice) {
        this.currentPetrolPrice = currentPetrolPrice;
    }

    public static Promise<PetrolPrice> getCurrentPetrolPrice(Agent agent, AID petrol) {
        return AgentHelper.requestInteraction(
                agent, petrol,
                ACLMessage.QUERY_REF, "currentPetrolPrice",
                null, PetrolPrice.class
        );
    }

    //region stationDescription

    public StationDescription getStationDescription() {
        return stationDescription;
    }

    public void setStationDescription(StationDescription stationDescription) {
        this.stationDescription = stationDescription;
    }

    public static Promise<StationDescription> getStationDescription(Agent me, AID station) {
        return AgentHelper.requestInteraction(me, station, ACLMessage.QUERY_REF, "getStationDescription", null, StationDescription.class);
    }

    //endregion

    public static Promise<DFAgentDescription[]> findAll(Agent agent) {
        return AgentHelper.findAllOf(agent, "petrolStation");
    }

    public static Promise<DFAgentDescription[]> findByUniqueName(Agent agent, String uniqueName) {
        return AgentHelper.findAllOf(agent, "petrolStation", uniqueName);
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

}
