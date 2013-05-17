package models;

import java.util.Date;

import javax.persistence.Entity;

import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;

@Entity
public class HaUser extends Model {
	
	@Required
	@Unique
	@MinSize(value=6)
	@MaxSize(value=80)
	@Email
	public String email;
	
	@Required
	@MinSize(value=6)
	@MaxSize(value=256)
	public String password;
	
	@Unique
	public String nickname;
	public String fullname;
	
	public Long fbId;
	public String fbName;
	public String fbLink;
	
	public Boolean pwSetFlg;
	
	public boolean isAdmin;
	
	public Boolean zero_hidden_bkem;
	public Boolean zero_hidden_idepo;
	public Boolean inv_hidden_bkem;
	
	public HaUser(
			String email,
			String password,
			String nickname,
			String fullname,
			Long fbId,
			String fbName,
			String fbLink,
			boolean pwSetFlg,
			Boolean isAdmin
			) {
		this.email = email;
		this.password = password;
		this.fullname = fullname;
		this.nickname = nickname;
		this.fbId = fbId;
		this.fbName = fbName;
		this.fbLink = fbLink;
		this.pwSetFlg = pwSetFlg;
		this.isAdmin = isAdmin;
	}

    public static HaUser connect(String email, String password) {
    	return find("byEmailAndPassword", email, password).first();
    }
    
	public String toString() {
        return email;
	}
}
