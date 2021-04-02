package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.JumboTailUser;
import com.crio.jumbotail.assettracking.exchanges.request.CreateUserRequest;
import com.crio.jumbotail.assettracking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCreationServiceImpl implements UserCreationService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public JumboTailUser save(CreateUserRequest user) {
		final JumboTailUser newUser = new JumboTailUser(
				user.getUsername(),
				passwordEncoder.encode(user.getPassword()),
				user.getRole()
		);

		return userRepository.save(newUser);
	}

}
