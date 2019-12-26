package choq.carbon.cfg;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import cho.carbon.cfg.condition.FgCondition;
import cho.carbon.cfg.imbean.ImportBeanController;
import cho.carbon.cfg.mq.SendMQListener;

//@Configuration
@ConditionalOnWebApplication //web应用才生效
@EnableConfigurationProperties(FGProperties.class)
@Import({SendMQListener.class, ImportBeanController.class, Jackson2JsonMessageConverter.class})
public class FGServerConfiguration {
    
}
