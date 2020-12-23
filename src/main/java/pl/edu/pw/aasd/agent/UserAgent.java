package pl.edu.pw.aasd.agent;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import org.javatuples.Pair;
import pl.edu.pw.aasd.AgentWithFace;
import pl.edu.pw.aasd.Jsonable;
import pl.edu.pw.aasd.data.PetrolPrice;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UserAgent extends AgentWithFace {

    @Override
    protected void setup() {
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


    }

    @Override
    protected Jsonable parseData(String name) {
        return null;
    }


    public void setPetrolStationPrice(AID petrolStation, PetrolPrice petrolPrice) {

    }

    public void voteOnStation(AID petrolStation, PetrolPrice petrolPrice) {

    }

}
