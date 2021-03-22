package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.JumboTailUser;
import com.crio.jumbotail.assettracking.exchanges.request.CreateUserRequest;

public interface UserCreationService {

	public JumboTailUser save(CreateUserRequest user);

}
