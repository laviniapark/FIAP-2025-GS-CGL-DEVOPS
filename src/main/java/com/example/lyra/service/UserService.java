package com.example.lyra.service;

import com.example.lyra.dto.request.UserRequest;
import com.example.lyra.dto.response.UserResponse;
import com.example.lyra.exception.ResourceNotFoundException;
import com.example.lyra.model.EHumor;
import com.example.lyra.model.ERole;
import com.example.lyra.model.Role;
import com.example.lyra.model.User;
import com.example.lyra.repository.RoleRepository;
import com.example.lyra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todos os usuários com paginação
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário nao encontrado com id: " + id));
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(UserRequest userRequest) {
        // Verifica se o email já está em uso
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Erro: Email já está em uso!");
        }

        // Cria um novo usuário
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        // Define o humor se fornecido
        if (userRequest.getHumor() != null) {
            user.setHumor(EHumor.fromCodigo(userRequest.getHumor()));
        }
        
        // Define a descrição do humor se fornecida
        if (userRequest.getHumorDescricao() != null) {
            user.setHumorDescricao(userRequest.getHumorDescricao());
        }
        
        // Define as permissões do usuário
        Set<Role> roles = new HashSet<>();
        if (userRequest.getRoles() == null || userRequest.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Erro: Permissão não encontrada."));
            roles.add(userRole);
        } else {
            userRequest.getRoles().forEach(role -> {
                if ("admin".equals(role)) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Erro: Permissão não encontrada."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Erro: Permissão não encontrada."));
                    roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
        
        // Atualiza os campos do usuário
        if (userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }
        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userRequest.getEmail())) {
                throw new RuntimeException("Erro: Email já está em uso!");
            }
            user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        if (userRequest.getHumor() != null) {
            user.setHumor(EHumor.fromCodigo(userRequest.getHumor()));
        }

        if (userRequest.getHumorDescricao() != null) {
            user.setHumorDescricao(userRequest.getHumorDescricao());
        }
        
        // Atualiza as permissões se fornecidas
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            userRequest.getRoles().forEach(role -> {
                if ("admin".equals(role)) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Erro: Permissão não encontrada."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Erro: Permissão não encontrada."));
                    roles.add(userRole);
                }
            });
            user.setRoles(roles);
        }
        
        User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public com.example.lyra.dto.response.DailyCheckInResponse saveDailyCheckIn(
            Long userId,
            com.example.lyra.dto.request.DailyCheckInRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + userId));

        // Atualiza os dados do check-in diário
        if (request.getHumor() != null) {
            user.setHumor(EHumor.fromCodigo(request.getHumor()));
        }

        if (request.getSono() != null) {
            user.setSono(request.getSono());
        }

        if (request.getHidratacao() != null) {
            user.setHidratacao(request.getHidratacao());
        }

        User savedUser = userRepository.save(user);
        return convertToDailyCheckInResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public com.example.lyra.dto.response.DailyCheckInResponse getDailyCheckIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + userId));

        return convertToDailyCheckInResponse(user);
    }

    private com.example.lyra.dto.response.DailyCheckInResponse convertToDailyCheckInResponse(User user) {
        com.example.lyra.dto.response.DailyCheckInResponse response =
                new com.example.lyra.dto.response.DailyCheckInResponse();

        if (user.getHumor() != null) {
            response.setHumor(user.getHumor().getCodigo());
            response.setHumorDescricao(user.getHumor().getDescricao());
        }

        response.setSono(user.getSono());
        response.setHidratacao(user.getHidratacao());

        // Mensagem motivacional baseada na hidratação
        if (user.getHidratacao() != null && user.getHidratacao() >= 8) {
            response.setMensagem("🎉 Parabéns! Você já atingiu a meta mínima diária de água!");
        } else if (user.getHidratacao() != null && user.getHidratacao() >= 5) {
            response.setMensagem("💧 Você está no caminho certo! Continue se hidratando!");
        } else if (user.getHidratacao() != null && user.getHidratacao() > 0) {
            response.setMensagem("💪 Bom começo! Lembre-se de beber mais água ao longo do dia.");
        }

        return response;
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
                
        // Define o humor e descrição na resposta
        if (user.getHumor() != null) {
            response.setHumor(user.getHumor().name());
            response.setHumorDescricao(user.getHumorDescricao());
        }
        return response;
    }
}
