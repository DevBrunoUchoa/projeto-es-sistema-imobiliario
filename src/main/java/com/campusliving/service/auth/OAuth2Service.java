package com.campusliving.service.auth;

import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2Service extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String nome = (String) attributes.get("name");

        List<User> existingUsers = userRepository.findByEmail(email);
        User user;

        if (existingUsers.isEmpty()) {
            user = User.builder()
                    .nome(nome != null ? nome : email.split("@")[0])
                    .email(email)
                    .senhaHash("")
                    .tipoConta(User.Tipo.ESTUDANTE)
                    .aceiteLgpd(true)
                    .verificado(true)
                    .ativo(true)
                    .build();
            userRepository.save(user);
        } else {
            user = existingUsers.get(0);
        }

        return oAuth2User;
    }
}