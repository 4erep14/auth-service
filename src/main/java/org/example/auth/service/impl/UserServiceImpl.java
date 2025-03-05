package org.example.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.auth.entity.User;
import org.example.auth.repository.UserRepository;
import org.example.auth.service.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public User getUserById(Long id) throws UsernameNotFoundException {
        return userRepository.findById(id).orElseThrow(() ->
                new UsernameNotFoundException("User with id " + id + " not found")
        );
    }
}
