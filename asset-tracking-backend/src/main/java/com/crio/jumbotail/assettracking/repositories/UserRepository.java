package com.crio.jumbotail.assettracking.repositories;


import com.crio.jumbotail.assettracking.entity.JumboTailUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<JumboTailUser, Long> {
	Optional<JumboTailUser> findByUsername(String user);
}