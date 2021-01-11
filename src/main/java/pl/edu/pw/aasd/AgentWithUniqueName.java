package pl.edu.pw.aasd;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import pl.edu.pw.aasd.promise.Promise;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AgentWithUniqueName extends Agent {

    @Override
    protected void setup() {

        var args = this.getArguments();
        if (args.length != 1 || !(args[0] instanceof String)) {
            System.err.println("Brak nazwy agenta");
            throw new RuntimeException();
        }

        AgentHelper.setupRequestResponder(this,
                ACLMessage.QUERY_REF, "getUniqueName",
                msg -> Promise.fulfilled(new JsonPrimitive(this.getUniqueName()))
        );
    }

    public String getUniqueName() {
        return this.getArguments()[0].toString();
    }

    public static Promise<String> getUniqueName(Agent agent, AID aid) {
        return AgentHelper.requestInteraction(
                agent, aid,
                ACLMessage.QUERY_REF, "getUniqueName",
                null
        ).thenApply(JsonElement::getAsString);
    }
}
