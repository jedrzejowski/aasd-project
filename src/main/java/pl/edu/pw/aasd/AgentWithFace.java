package pl.edu.pw.aasd;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import pl.edu.pw.aasd.agent.PetrolStationAgent;
import pl.edu.pw.aasd.promise.Promise;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public abstract class AgentWithFace extends Agent {
    private HttpPingServer httpPingServer;

    public AgentWithFace() {
        super();

        SafeThread.run(this::basicSetup, 100);
    }

    void basicSetup() {

        this.httpPingServer = new HttpPingServer();

        this.httpPingServer.handleFile("/", "agentFace/" + this.getClass().getSimpleName() + ".html");
        this.staticFilesSetup("/", "agentFace/");

        this.httpPingServer.handle("/name", body -> Promise.fulfilled(Jsonable.toString(this.getAID().getName())));
        this.httpPingServer.handle("/class", body -> Promise.fulfilled(Jsonable.toString(this.getClass().getName())));

        this.setupPetrolStationCommon();

        this.setupFace();

        System.out.printf("Face of '%s' started at http://localhost:%d\n",
                this.getName(), this.httpPingServer.getSocketNum());
    }

    void staticFilesSetup(String base, String path) {

        try {
            var uri = getClass().getClassLoader().getResource("agentFace").toURI();
            var inodes = new File(uri).list();

            assert inodes != null;

            Arrays.stream(inodes).forEach(filename -> {
                this.httpPingServer.handleFile(
                        Paths.get(base, filename).toString(),
                        Paths.get(path, filename).toString()
                );
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected abstract void setupFace();

    public interface FacePingHandler {
        JsonElement handle(JsonElement body) throws Throwable;
    }

    protected void faceHandle(String path, FacePingHandler handler) {
        httpPingServer.handle(path, body -> {
            JsonElement parsed = body != null ? JsonParser.parseString(body) : null;
            var response = handler.handle(parsed);
            return Promise.fulfilled(response != null ? response.toString() : "null");
        });
    }

    void setupPetrolStationCommon() {
        httpPingServer.handle("/api/petrolStation/getAll", body -> {
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

            return Promise.fulfilled(Jsonable.toString(names));
        });

        httpPingServer.handle("/api/petrolStation/currentPetrolPrice", petrolStationName -> {
            var petrolStation = new AID(petrolStationName, true);
            var response = new JsonObject();

            var petrolPrice = PetrolStationAgent.getCurrentPetrolPrice(this, petrolStation).get();

            response.addProperty("name", petrolStationName);
            response.add("petrolPrice", petrolPrice.toJson());

            return Promise.fulfilled(response.toString());
        });


        httpPingServer.handle("/api/petrolStation/stationDescription",
                petrolStationName -> new Promise<String>().fulfillInAsync(() -> {

                    var petrolStation = new AID(petrolStationName, true);

                    var petrolPrice = PetrolStationAgent.getStationDescription(this, petrolStation).get();

                    return petrolPrice.toString();
                }));
    }
}
