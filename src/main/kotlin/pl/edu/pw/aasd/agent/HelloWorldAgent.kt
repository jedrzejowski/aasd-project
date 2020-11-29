package pl.edu.pw.aasd.agent

import jade.core.Agent;

class HelloWorldAgent : Agent() {
    override fun setup() {
        System.out.println("Hello World! My name is " + getLocalName())

        // Make this agent terminate
        doDelete()
    }
}