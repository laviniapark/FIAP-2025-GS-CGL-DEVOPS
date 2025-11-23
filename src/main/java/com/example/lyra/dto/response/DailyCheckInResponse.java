package com.example.lyra.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCheckInResponse {
    private Integer humor; // 1-5
    private String humorDescricao; // Descrição do humor (Ótimo, Bem, etc.)
    private Integer sono; // Horas de sono
    private Integer hidratacao; // Copos de água
    private String mensagem;
}
