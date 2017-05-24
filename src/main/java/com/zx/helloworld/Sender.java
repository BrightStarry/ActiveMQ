package com.zx.helloworld;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * ActiveMQ 消息发送者
 */
public class Sender {

    public static void main(String[] args) throws Exception{
        //1. 建立ConnectionFactory工厂对象，需要用户名、密码，以及连接地址，
        //默认地址为 tcp://localhost:61616
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
//                ActiveMQConnectionFactory.DEFAULT_USER,
//                ActiveMQConnectionFactory.DEFAULT_PASSWORD,
                "zx",
                "123456",
                "tcp://localhost:61616"
        );

        //2. 通过ConnectionFactory创建一个Connection,并使用start()开启连接，连接默认是关闭的
        Connection connection = connectionFactory.createConnection();
        connection.start();

        //3. 通过Connection创建session,参数1是是否启用事务，参数2是设置签收模式，一般为自动签收
//        Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);

        //使用事务进行消息发送
//        Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);

        //使用Client端签收的方式，也就是消费者需要调用方法签收，才算消息完成
        Session session = connection.createSession(Boolean.TRUE, Session.CLIENT_ACKNOWLEDGE);

        //4. 通过session创建Destination（目的地）,PTP中为queue，pub/sub中为topic（主题）
        Destination destination = session.createQueue("queue1");

        //5. 通过session创建消息生产者/消息消费者;MessageProducer/MessageConsumer
//        MessageProducer messageProducer = session.createProducer(destination);
        MessageProducer messageProducer = session.createProducer(null);//可以在send方法中指定

        //6. 设置MessageProducer的持久化特性,
//        messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);//可以在send方法中指定


        //7. 发送消息
        for (int i =0; i < 5; i++) {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText("收到通知，石楠花开，呼我一脸,id:" + i);
            /**
             * 1. 目的地
             * 2. 消息
             * 3. 持久化模式
             * 4. 消息优先级 0-9
             * 5. 消息过期时间 ms
             */
            messageProducer.send(destination,textMessage,DeliveryMode.NON_PERSISTENT,1,-1);
        }

        //使用事务时，需要提交
        session.commit();

        //关闭connection会自动关闭session
        if(connection != null){
            connection.close();
        }
    }
}
