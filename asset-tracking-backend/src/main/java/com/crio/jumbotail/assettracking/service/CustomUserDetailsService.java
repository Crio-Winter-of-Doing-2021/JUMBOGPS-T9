package com.crio.jumbotail.assettracking.service;


import com.crio.jumbotail.assettracking.entity.JumboTailUser;
import com.crio.jumbotail.assettracking.exchanges.request.CreateUserRequest;
import com.crio.jumbotail.assettracking.repositories.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.map(user -> {
					final List<SimpleGrantedAuthority> roles = Arrays.stream(user.getRoles().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

					return new User(user.getUsername(), user.getPassword(), roles);
				})
				.orElseThrow(() -> new UsernameNotFoundException("User not found with the name " + username));

	}

	public JumboTailUser save(CreateUserRequest user) {
		final JumboTailUser newUser = new JumboTailUser(
				user.getUsername(),
				passwordEncoder.encode(user.getPassword()),
				user.getRole()
		);

		return userRepository.save(newUser);
	}

}
