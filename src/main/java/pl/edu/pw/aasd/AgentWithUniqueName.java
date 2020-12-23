package pl.edu.pw.aasd;

import jade.core.Agent;

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

    }

    public String getUniqueName() {
        return this.getArguments()[0].toString();
    }
}
