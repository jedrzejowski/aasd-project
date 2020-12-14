package pl.edu.pw.aasd;

import jade.lang.acl.MessageTemplate;

public class MessageTemplate2 {

    public static MessageTemplate and(
            MessageTemplate m1,
            MessageTemplate m2,
            MessageTemplate m3
    ) {
        return MessageTemplate.and(MessageTemplate.and(m1, m2), m3);
    }

}
