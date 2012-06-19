//  The contents of this file are subject to the Mozilla Public License
//  Version 1.1 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License
//  at http://www.mozilla.org/MPL/
//
//  Software distributed under the License is distributed on an "AS IS"
//  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
//  the License for the specific language governing rights and
//  limitations under the License.
//
//  The Original Code is RabbitMQ.
//
//  The Initial Developer of the Original Code is VMware, Inc.
//  Copyright (c) 2007-2012 VMware, Inc.  All rights reserved.
//

package com.rabbitmq.examples;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Java Application which receives and prints all messages sent to a topic exchange.
 */
public class LogTail {
    /**
     * @param args command line parameters:
     * <p>
     * Zero, one or two positional parameters:
     * </p>
     * <ul>
     * <li><i>AMQP-uri</i> -
     * the AMQP uri to connect to the broker to use. Default <code>amqp://localhost</code>.
     * (See {@link ConnectionFactory#setUri(String) setUri()}.)
     * </li>
     * <li><i>exchange</i> - the (topic) exchange to subscribe to.
     * Default <code>amq.rabbitmq.log</code>.
     * </li>
     * </ul>
     */
    public static void main(String[] args) {
        try {
            String uri = (args.length > 0) ? args[0] : "amqp://localhost";
            String exchange = (args.length > 1) ? args[1] : "amq.rabbitmq.log";

            ConnectionFactory cfconn = new ConnectionFactory();
            cfconn.setUri(uri);
            Connection conn = cfconn.newConnection();

            Channel ch1 = conn.createChannel();

            String queueName = ch1.queueDeclare().getQueue();
            ch1.queueBind(queueName, exchange, "#");

            QueueingConsumer consumer = new QueueingConsumer(ch1);
            ch1.basicConsume(queueName, true, consumer);
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String routingKey = delivery.getEnvelope().getRoutingKey();
                String contentType = delivery.getProperties().getContentType();
                System.out.println("Content-type: " + contentType);
                System.out.println("Routing-key: " + routingKey);
                System.out.println("Body:");
                System.out.println(new String(delivery.getBody()));
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("Main thread caught exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
