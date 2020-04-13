package cho.carbon.cfg.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import cho.carbon.utils.MessageDTO;


public class SendMQListener implements ApplicationListener<ContextRefreshedEvent>{

	 Logger   logger = LoggerFactory.getLogger(SendMQListener.class);
	
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	MessageDTO message;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// 容器刷新完成的时候， 发消息给消息中间件
		rabbitTemplate.convertAndSend("carbon.fg", "carbonqueukey", message);
		logger.debug("容器完成： " + event.toString());
	}

}
