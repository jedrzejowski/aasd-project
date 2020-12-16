package pl.edu.pw.aasd;

import com.google.gson.JsonObject;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import pl.edu.pw.aasd.agent.PetrolStationAgent;
import pl.edu.pw.aasd.promise.Promise;

import java.util.Arrays;

public abstract class AgentWithFace extends Agent {
    protected HttpPingServer httpPingServer;

    public AgentWithFace() {
        super();

        SafeThread.run(this::basicSetup, 100);
    }

    void basicSetup() {

        this.httpPingServer = new HttpPingServer();

        this.httpPingServer.handleFile("/", "index.html");
        this.httpPingServer.handleFile("/index.html", "index.html");
        this.httpPingServer.handleFile("/jquery.js", "jquery.js");
        this.httpPingServer.handleFile("/index.js", "index.js");
        // this.httpPingServer.handleFile("/boostrap.css", "boostrap.css");

        this.httpPingServer.handle("/name", body -> Promise.fulfilled(Jsonable.toJson(this.getAID().getName())));
        this.httpPingServer.handle("/class", body -> Promise.fulfilled(Jsonable.toJson(this.getClass().getName())));

        httpPingServer.handle("/getPetrolStations", body -> {
            var descriptions = PetrolStationAgent.findAll(this).get();

            var names = Arrays.stream(descriptions)
                    .map(DFAgentDescription::getName)
                    .map(AID::getName)
                    .map(name -> {
                        var obj = new JsonObject();
                        obj.addProperty("name", name);
                        return obj;
                    })
                    .toArray();

            return Promise.fulfilled(Jsonable.toJson(names));
        });

        httpPingServer.handle("/getPetrolStation", petrolStationName -> {
            var petrolStation = new AID(petrolStationName, true);
            var response = new JsonObject();

            var petrolPrice = PetrolStationAgent.getCurrentPrice(this, petrolStation).get();

            response.addProperty("name", petrolStationName);
            response.add("petrolPrice", petrolPrice.toJson());

            return Promise.fulfilled(response.toString());
        });


        this.setupPings();

        System.out.printf("Face of '%s' started at http://localhost:%d\n",
                this.getName(), this.httpPingServer.getSocketNum());
    }

    protected abstract void setupPings();

}
