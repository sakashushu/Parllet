package models;

import java.util.Date;

import javax.persistence.Entity;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class HaUser extends Model {

	@Email
	@Required
	public String email;

	@Required
	public String password;

	public String fullname;
	public boolean isAdmin;
	
	public HaUser(
			String email,
			String password,
			String fullname,
			boolean isAdmin
			) {
		this.email = email;
		this.password = password;
		this.fullname = fullname;
		this.isAdmin = isAdmin;
	}

	public String toString() {
        return email;
	}
}
