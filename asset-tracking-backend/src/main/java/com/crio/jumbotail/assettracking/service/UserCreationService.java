package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.JumboTailUser;
import com.crio.jumbotail.assettracking.exchanges.request.CreateUserRequest;

/**
 * To Create a new user
 */
public interface UserCreationService {

	/**
	 *
	 * @param user the user to be created
	 * @return the created user entity
	 */
	JumboTailUser save(CreateUserRequest user);

}
