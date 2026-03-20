package br.com.thomas.library.rental_service.service;

import br.com.thomas.library.rental_service.dto.response.PagedUserResponse;
import br.com.thomas.library.rental_service.dto.response.UserResponse;
import br.com.thomas.library.rental_service.model.User;
import br.com.thomas.library.rental_service.model.UserProfile;
import br.com.thomas.library.rental_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Lista usuários com perfil USER (clientes), para visualização do gestor.
     */
    public PagedUserResponse listClients(Pageable pageable) {
        Page<User> page = userRepository.findByProfileAndActiveTrue(UserProfile.USER, pageable);
        return PagedUserResponse.builder()
                .content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .active(user.getActive())
                .build();
    }
}
