package com.zx.helloworld;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * ActiveMQ 消息接收者
 */
public class Receiver {

    public static void main(String[] args) throws Exception{
        //1. 建立ConnectionFactory工厂对象，需要用户名、密码，以及连接地址，
        //默认地址为 tcp://localhost:61616
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                /*ActiveMQConnectionFactory.DEFAULT_USER,
                ActiveMQConnectionFactory.DEFAULT_PASSWORD,*/
                "zx",
                "123456",
                "tcp://localhost:61616"
        );

        //2. 通过ConnectionFactory创建一个Connection,并使用start()开启连接，连接默认是关闭的
        Connection connection = connectionFactory.createConnection();
        connection.start();

        //3. 通过Connection创建session,参数1是是否启用事务，参数2是设置签收模式，一般为自动签收
        Session session = connection.createSession(Boolean.FALSE, Session.CLIENT_ACKNOWLEDGE);

        //4. 通过session创建Destination（目的地）,PTP中为queue，pub/sub中为topic（主题）
        Destination destination = session.createQueue("queue1");

        //5. 通过session创建消息生产者/消息消费者;MessageProducer/MessageConsumer
        MessageConsumer messageConsumer = session.createConsumer(destination);

        /**
         * 一直阻塞，等待消息
         * receive()方法可以设置为一直阻塞，也可以传入long设置成等待一段时间，也可以只取一下，有就返回。
         */
        while(true){
            TextMessage message = (TextMessage)messageConsumer.receive();
            //如果使用的签收模式为客户端签收，需要再调用下这个方法实现签收。
            message.acknowledge();
            if(message == null) break;
            System.out.println("收到消息，内容为:" + message.getText());

        }
        
        //关闭connection会自动关闭session
        if(connection != null){
            connection.close();
        }
    }
}
