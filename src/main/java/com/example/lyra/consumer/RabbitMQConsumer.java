package com.example.lyra.consumer;

import com.example.lyra.dto.HumorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por consumir mensagens da fila de atualizações de humor
 */
@Service
public class RabbitMQConsumer {

    // Logger para registrar as operações
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    /**
     * Processa as mensagens recebidas da fila de humor
     * @param message Objeto contendo os dados do humor do usuário
     */
    @RabbitListener(queues = "${rabbitmq.queue.humor}")
    public void consume(HumorMessage message) {
        // Loga as informações da mensagem recebida
        LOGGER.info(String.format("Mensagem recebida -> Usuário: %s, Humor: %s, Descrição: %s, Data/Hora: %s",
                message.getUserName(),
                message.getHumor(),
                message.getDescricao(),
                message.getDataHora()));
    }
}
