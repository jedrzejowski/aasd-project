package pl.edu.pw.aasd.agent;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jade.core.AID;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PetrolPrice;
import pl.edu.pw.aasd.data.StationDescription;
import pl.edu.pw.aasd.data.VehicleData;

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
    }


    public void setPetrolStationPrice(AID petrolStation, PetrolPrice petrolPrice) {

    }

    public void voteOnStation(AID petrolStation, PetrolPrice petrolPrice) {

    }

}
