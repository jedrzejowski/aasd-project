package pl.edu.pw.aasd.agent;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import pl.edu.pw.aasd.AgentHelper;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.AgentWithUniqueName;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.Near;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.data.VehicleData;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class UserAgent extends AgentWithFace<UserAgent.MyData> {

    class MyData extends Jsonable {
        VehicleData vehicleData = new VehicleData();

    }

    @Override
    protected MyData parseData(String data) {
        return data == null ? new MyData() : Jsonable.from(data, MyData.class);
    }

    @Override
    protected void setup() {
        super.setup();
//
//        new Thread(() -> {
//            while (true) {
//                try {
//                    Thread.sleep(2000);
//
//                    var descriptions = PetrolStationAgent.findAll(this).get();
//
//                    System.out.println(
//                            Arrays.stream(descriptions)
//                                    .map(DFAgentDescription::getName)
//                                    .map(petrol -> {
//                                        PetrolPrice price = null;
//
//                                        try {
//                                            price = PetrolStationAgent.getCurrentPrice(this, petrol).get();
//                                        } catch (Exception ignored) {
//                                        }
//
//                                        return Pair.with(petrol, price);
//                                    })
//                                    .filter(pair -> pair.getValue1() != null)
//                                    .map(pair -> pair.getValue0().toString() + " " + pair.getValue1())
//                                    .collect(Collectors.joining("\n"))
//                    );
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    System.out.println(e.toString());
//                }
//            }
//        }).start();

//        PetrolStationAgent.getCurrentPetrolPrice(this, petrolAID);

        this.handleHttpApi("/api/this/getVehicleData",
                body -> Jsonable.toJson(this.data.vehicleData));

        this.handleHttpApi("/api/this/setVehicleData", body -> {
            var request = body.getAsJsonObject();
            this.data.vehicleData = Jsonable.from(request, VehicleData.class);
            return new JsonPrimitive(true);
        });

        this.handleHttpApi("/api/this/findNearPetrolStation", body -> {
            var near = new Near(
                    this.data.vehicleData.getLatitude(),
                    this.data.vehicleData.getLongitude(),
                    1000 // TODO wyliczyć
            );
            var descriptions = PetrolStationAgent.findNear(this, near);

            var names = Arrays.stream(descriptions)
                    .map(DFAgentDescription::getName)
                    .map(aid -> {
                        var obj = new JsonObject();
                        obj.addProperty("name", aid.getName());

                        try {
                            var uniqueNamePromise = AgentWithUniqueName.getUniqueName(this, aid);
                            var stationDescriptionPromise = PetrolStationAgent.getStationDescription(this, aid);
                            var petrolPricePromise = PetrolStationAgent.getCurrentPetrolPrice(this, aid);

                            obj.addProperty("uniqueName", uniqueNamePromise.get());
                            obj.add("stationDescription", stationDescriptionPromise.get().toJson());
                            obj.add("petrolPrice", petrolPricePromise.get().toJson());
                        } catch (Throwable ignore) {
                            return null;
                        }

                        return obj;
                    })
                    .filter(Objects::nonNull)
                    .toArray();

            return Jsonable.toJson(names);
        });
    }


    public void setPetrolStationPrice(AID petrolStation, PetrolPrice petrolPrice) {

    }

    public void voteOnStation(AID petrolStation, PetrolPrice petrolPrice) {

    }

}
