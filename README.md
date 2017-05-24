####ActiveMQ 消息队列
---
#####JMS java消息服务
jms之前的RPC(远程过程调用)框架,有局限性：
1. 同步通信
2. 客户和服务对像声明周期紧密，双方都必须正常运行，否则会异常。
3. 点对点，客户的一次调用只能发送送给单独的目标对象。

消息中间件很好的解决了以上问题。发送者将消息发送给消息服务器，消息服务器将消息存放在若干队列中，
在合适的时候将消息转发给接收者。可以实现异步，接收消息的时候发送者也不一定是运行的，以及一对多通讯。

JMS定义了消息中间件的接口，JMS只是接口，没有实现。实现JMS接口的消息中间件被成为JMS Provider。

术语:  provider（生产者）、consumer(消费者)、PTP(point to point，点对点)、
pub/sub（publish/subscribe，发布/订阅）、topic（主题,pub/sub时使用）、
ConnectionFactory(连接工厂，JMS用它创建连接)、
Connection(jms client与provider的连接)、
Destination(目标，消息目的地)、
MessageConsumer(消费者接口)、
MessageProducer(生产者接口)、
Message(消息接口)，一般分为消息头（必须）、消息属性（可选）、
消息体（可选，文本、映射、字节、流、对象物种消息类型）
StreamMessage、MapMessage、TextMessage、ObjectMessage、ByteMessage、BlobMessage 
Session(会话，一个发送或接收消息的线程)

pub/sub方式，客户端无法收到在它连接到对应Topic之前，该生产者发送的数据。

---
---
####启动方法
下载ActiveMQ,windows版本。运行bin中64位中的bat文件。
http://localhost:8161,访问这个地址进入管理页面，默认用户名密码为admin,可以在jetty-realm.properties中配置。
另外其他一些配置在activemq.xml中。

然后具体见helloworld包中的小例子。

---
---
####安全认证
在activemq.xml文件的<broker>标签中加入
        <plugins>
			<simpleAuthenticationPlugin>
				<users>
					<authenticationUser username="zx" password="123456" groups="users,admins"/>
				</users>
			</simpleAuthenticationPlugin>
		</plugins>
这里的用户密码就是创建ConnectionFactory时传入的用户名和密码。
---
---
使用事务的话，需要使用session.commit()提交事务.

签收模式有三种:(生产者和消费者都需要设置)
1. Session.AUTO_ACKNOWLEDGE 当客户端receive()或onMessage成功返回时，session自动签收这条消息
2. Session.CLIENT_ACKNOWLEDGE 其客户端调用Message的acknowledge()方法签收消息。
这种情况下，签收发生在session层面：签收一个已经消费的消息，会自动签收这个session所有已经消费的收条。
3. Session.DUPS_OK_ACKNOWLEDGE session不用确保消息的签收，可能会引起消息的重复（多个消费者的并发场景，获取到同一条消息），但是降低了session的开销。


---
MessageProducer （消息生产者类），是一个由Session创建的对象，用来向Destination发送消息.
send()方法中可以指定：
消息优先级（0-9 0-4普通，5-9加急，不指定为4。无法保证？保证加急的比普通优先，开启优先级需要在配置文件中）。
消息过期时间（默认永不过期）。
持久化模式（DeliveryMode，默认为持久化成文件）：
1. PERSISTENT 
2. NON_PERSISTENT  不做持久化，

http://www.cnblogs.com/tommyli/archive/2010/09/13/1825205.html（持久化配置方法）
注意，目前activemq的lib中使用是的common-dbcp2，所以，要org.apache.commons.dbcp2.BasicDataSource，加个2，
且maxActive修改成了maxTotal
---
####MessageConsumer(消息消费类)是一个由Session创建的对象，用来从Destination接收消息。
createConsumer()方法，除了Destination,还可以指定MessageSelector(消息选择器),Topic（主题）和主题名字（持久化时用），以及noLocal.

noLocal默认为false，为true时只能接收和自己相同Connection所发布的消息。noLocal只适用于Topic，不适用于Queue.

MessageSelector:
如果这么设置createConsumer(destination,"a='aaa''") ;（类似sql where的写法，可以and or > < ）
检查消息的a属性，并确定这个属性值是否等于aaa,相等才消费，否则忽略。
!注意，使用这个的时候，传入的消息就是MapMessage(类似HashMap),且给这个消息设置值的时候，不能使用
setString()这样的方法，必须使用setStringProperty()这样的方法，才能使选择器生效。
---
消息同步接收:
使用messageConsumer.receive()/receive(long timeout)/receiveNoWait()方法。
会阻塞自己

消息异步接收:
设置监听器，收到消息时自动触发监听器对象的onMessage()方法
messageConsumer.setMessageListener(new A());
A类是一个自定义的实现了MessageListener接口的类，这个接口定义了onMessage(Message message)方法
---
####创建临时消息
createTemporaryQueue()和createTemporaryTopic()创建临时的目标。
持续到创建它的Connection关闭（内容也消息），只有创建它的Connection所创建的客户端才可以从临时目标中接收消息。
但任何生产者都可以向它发送消息。
---
###整合spring
http://elim.iteye.com/blog/1893038
这块我不想做了，简单得很。spring也没提供什么封装的，基本直接把bean托管给spring吗。

---
---
###Zookeeper + LevelDB + ActiveMQ 搭建集群。 就不实践了
1. 使用zookeeper实现Master-Slave。使用zookeeper（集群）注册所有的ActiveMQ Broker(节点),
只有一个broker可以对外提供服务(也就是Master)，其他的broker处于待机状态（Slave）.如果Master
宕机，就从Slave选举出一个broker充当master节点。

http://blog.csdn.net/wenniuwuren/article/details/45105161

具体可以看activemq集群配置文档.pdf这个文档，就在项目里

然后在代码中，要将连接的url改成:
failover:(tcp://192.168.2.104:51511,tcp://xxxxxxxxxxxxxx,tcp://xxxxxxxxxxxxxxx)?Randomize=false

###还可以使用网络模式进行集群搭建，实现队列等共享。文档也在项目里。



