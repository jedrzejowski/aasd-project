package pl.edu.pw.aasd;

public class Boot {

    static public boolean DEBUG_CREATE_CHILDREN = true;

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
