package com.example.lyra.service;

import com.example.lyra.dto.HumorMessage;
import com.example.lyra.model.EHumor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Serviço responsável por publicar mensagens na fila de atualizações de humor
 */
@Service
public class RabbitMQProducer {

    // Logger para registrar as operações
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    // Nome do exchange configurado no RabbitMQ
    @Value("${rabbitmq.exchange}")
    private String exchange;

    // Chave de roteamento para as mensagens
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // Cliente para envio de mensagens ao RabbitMQ
    private final RabbitTemplate rabbitTemplate;

    /**
     * Construtor com injeção de dependência
     * @param rabbitTemplate Instância do RabbitTemplate para envio de mensagens
     */
    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Envia uma atualização de humor para a fila de mensagens
     * @param userId ID do usuário
     * @param userName Nome do usuário
     * @param humor Estado de humor do usuário
     * @param descricao Descrição adicional sobre o humor
     */
    public void sendHumorUpdate(Long userId, String userName, EHumor humor, String descricao) {
        // Registra o envio da mensagem no log
        LOGGER.info(String.format("Enviando atualização de humor para o usuário: %s", userName));
        
        // Cria a mensagem com os dados do usuário
        HumorMessage message = new HumorMessage(
            userId,
            userName,
            humor,
            descricao,
            LocalDateTime.now()
        );
        
        // Envia a mensagem para o exchange com a routing key especificada
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
