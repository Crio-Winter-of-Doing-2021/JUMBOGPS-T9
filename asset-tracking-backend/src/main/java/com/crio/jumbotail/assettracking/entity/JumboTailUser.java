package com.crio.jumbotail.assettracking.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jumbouser")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JumboTailUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String password;
	private String roles;

	public JumboTailUser(String username, String password, String roles) {
		this.username = username;
		this.password = password;
		this.roles = roles;
	}

}
