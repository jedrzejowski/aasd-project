package pl.edu.pw.aasd;

import java.util.ArrayList;

public class Boot {

    public static void main(String args[]) {
//        var args = new ArrayList<String>();

        var agents = "";

        agents += "Owner1:pl.edu.pw.aasd.agent.OwnerAgent[a,b];";

        agents += "Petrol1:pl.edu.pw.aasd.agent.PetrolStationAgent;";
        agents += "Petrol2:pl.edu.pw.aasd.agent.PetrolStationAgent;";


        jade.Boot.main(new String[]{
                "-gui",
                agents
        });


    }
}
