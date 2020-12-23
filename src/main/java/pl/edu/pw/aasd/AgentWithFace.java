package pl.edu.pw.aasd;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import pl.edu.pw.aasd.agent.PetrolStationAgent;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public abstract class AgentWithFace<Data extends Jsonable> extends AgentWithData<Data> {
    private HttpPingServer httpPingServer;


    @Override
    protected void setup() {
        super.setup();

        this.httpPingServer = new HttpPingServer();

        this.httpPingServer.handleFile("/", "agentFace/" + this.getClass().getSimpleName() + ".html");
        this.staticFilesSetup("/", "agentFace/");

        handleHttpApi("/name", body -> new JsonPrimitive(this.getAID().getName()));
        handleHttpApi("/class", body -> new JsonPrimitive(this.getClass().getName()));

        this.setupPetrolStationCommon();

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

    public interface HttpFaceHandler {
        JsonElement handle(JsonElement body) throws Throwable;
    }

    protected void handleHttpApi(String path, HttpFaceHandler handler) {
        httpPingServer.handle(path, body -> {
            JsonElement parsed = body != null ? JsonParser.parseString(body) : null;
            var response = handler.handle(parsed);
            return response != null ? response.toString() : "null";
        });
    }

    void setupPetrolStationCommon() {

        //region petrolStation

        handleHttpApi("/api/petrolStation/getAll", body -> {
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

            return Jsonable.toJson(names);
        });

        handleHttpApi("/api/petrolStation/isOnline", body -> {
            var petrolStationName = body.getAsString();

            try {
                var descs = PetrolStationAgent.findByUniqueName(this, petrolStationName).get();
                return new JsonPrimitive(true);
            } catch (Throwable e) {
                return new JsonPrimitive(false);
            }
        });

        handleHttpApi("/api/petrolStation/currentPetrolPrice", body -> {
            var petrolStationName = body.getAsString();
            var petrolStation = new AID(petrolStationName, true);
            var response = new JsonObject();

            var petrolPrice = PetrolStationAgent.getCurrentPetrolPrice(this, petrolStation).get();

            response.addProperty("name", petrolStationName);
            response.add("petrolPrice", petrolPrice.toJson());

            return response;
        });

        handleHttpApi("/api/petrolStation/stationDescription", body -> {
            var petrolStationName = body.getAsString();
            var petrolStation = new AID(petrolStationName, true);

            var petrolPrice = PetrolStationAgent.getStationDescription(this, petrolStation).get();

            return petrolPrice.toJson();
        });

        //endregion

        //region pylon

        handleHttpApi("/api/pylon/isOnline", body -> {
            var petrolStationName = body.getAsString();

            try {
                var descs = PetrolStationAgent.findByUniqueName(this, petrolStationName).get();
                return new JsonPrimitive(true);
            } catch (Throwable e) {
                return new JsonPrimitive(false);
            }
        });
    }
}
