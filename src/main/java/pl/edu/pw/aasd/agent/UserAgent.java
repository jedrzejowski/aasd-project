package pl.edu.pw.aasd.agent;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.AgentWithUniqueName;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.*;

import java.util.*;
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

        this.handleHttpApi("/api/this/reservePromotion", body -> {
            var promotion = Jsonable.from(body.getAsJsonObject(), PromotionReservationRequest.class);
            promotion.setUserId(this.getUniqueName());
            var result = reservePromotion(promotion);
            return new JsonPrimitive(result);
        });

        this.handleHttpApi("/api/this/getVehicleData",
                body -> Jsonable.toJson(this.data.vehicleData));

        this.handleHttpApi("/api/this/findPromotions", body -> {
            var descriptions = PartnerAgent.findAll(this);
            var names = Arrays.stream(descriptions)
                    .map(DFAgentDescription::getName)
                    .map(aid -> {
                        var obj = new JsonObject();
                        obj.addProperty("name", aid.getName());

                        try {
                            var uniqueNamePromise = AgentWithUniqueName.getUniqueName(this, aid);
                            var promotionsPromise = PartnerAgent.getPromotions(this, aid);
                            //var petrolPricePromise = PetrolStationAgent.getCurrentPetrolPrice(this, aid);

                            obj.addProperty("uniqueName", uniqueNamePromise.get());
                            obj.add("promotions", Jsonable.toJson(promotionsPromise.get()));

                        } catch (Throwable ignore) {
                            return null;
                        }

                        return obj;
                    })
                    .filter(Objects::nonNull)
                    .toArray();

            return Jsonable.toJson(names);
        });

        this.handleHttpApi("/api/this/setVehicleData", body -> {
            var request = body.getAsJsonObject();
            this.data.vehicleData = Jsonable.from(request, VehicleData.class);
            return new JsonPrimitive(true);
        });

        this.handleHttpApi("/api/this/findNearPetrolStation", body -> {
            var request = body.getAsJsonObject();
            var radiusTemp = 0.0f;
            try {
                radiusTemp = Jsonable.from(request, RadiusRequest.class).getRadius();
            } catch (Throwable e) {

            }
            var radius = radiusTemp;
            float radiusInGeoCoords = radius / (float) 78.471863174;
            var near = new Near(
                    this.data.vehicleData.getLatitude(),
                    this.data.vehicleData.getLongitude(),
                    radiusInGeoCoords
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
                            if (radius > 0) {
                                var stationDesc = stationDescriptionPromise.get();
                                if (countSquareDistance(
                                        stationDesc.getLatitude(),
                                        near.getLatitude(),
                                        stationDesc.getLongitude(),
                                        near.getLongitude()) > near.getDistance() * near.getDistance()
                                )
                                    return null;
                            }
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

        this.handleHttpApi("/api/this/findCheapestPetrolStation", body -> {
            var near = new Near(
                    this.data.vehicleData.getLatitude(),
                    this.data.vehicleData.getLongitude(),
                    0
            );

            var descriptions = PetrolStationAgent.findAll(this);

            if (descriptions.length == 0)
                return new JsonObject();

            class TempStruct extends Jsonable {
                double value;
                PetrolPrice petrolPrice;
                StationDescription stationDescription;
                String name;
                String uniqueName;

                public double getValue() {
                    return value;
                }
            }

            var stations = Arrays.stream(descriptions)
                    .map(DFAgentDescription::getName)
                    .map(aid -> {
                        try {
                            var tempStruct = new TempStruct();

                            var stationDescriptionPromise = PetrolStationAgent.getStationDescription(this, aid);
                            var petrolPricePromise = PetrolStationAgent.getCurrentPetrolPrice(this, aid);

                            var stationDesc = stationDescriptionPromise.get();
                            var petrolPrice = petrolPricePromise.get();
                            var userData = this.data.vehicleData;

                            var distance = Math.sqrt(countSquareDistance(stationDesc.getLatitude(), userData.getLatitude(), stationDesc.getLongitude(), userData.getLongitude()));
                            var fuelNeeded = distance * (float) 78.471863174 / 100 * userData.getFuelPerKilometer();

                            tempStruct.value = (userData.getFuelLeft() + fuelNeeded) * Double.parseDouble(petrolPrice.getPb95());
                            tempStruct.petrolPrice = petrolPrice;
                            tempStruct.stationDescription = stationDesc;
                            tempStruct.name = aid.getName();
                            tempStruct.uniqueName = AgentWithUniqueName.getUniqueName(this, aid).get();

                            return tempStruct;
                        } catch (Throwable exception) {
                            exception.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingDouble(TempStruct::getValue))
                    .toArray();

            var station = (TempStruct) stations[0];

            if (station == null) {
                return null;
            }

            var obj = new JsonObject();
            obj.addProperty("name", station.name);
            obj.addProperty("uniqueName", station.uniqueName);
            obj.add("stationDescription", station.stationDescription.toJson());
            obj.add("petrolPrice", station.petrolPrice.toJson());
            return obj;
        });
    }


    public void setPetrolStationPrice(AID petrolStation, PetrolPrice petrolPrice) {

    }

    public void voteOnStation(AID petrolStation, PetrolPrice petrolPrice) {

    }

    private boolean reservePromotion(PromotionReservationRequest promotion) {
        try {
            return PartnerAgent.reservePromotion(this, promotion.getPartner(), promotion.toJson()).get();
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private static double countSquareDistance(double lat1, double lat2, double long1, double long2) {
        var temp1 = (lat1 - lat2);
        var temp2 = (long1 - long2);
        return temp1 * temp1 + temp2 * temp2;
    }

}
