package leonardomarcus.com.br.pathwheel.api.response;

import leonardomarcus.com.br.pathwheel.api.model.User;

public class AuthenticateResponse extends Response {
	private User user;
	private String authorizationToken;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getAuthorizationToken() {
		return authorizationToken;
	}
	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}
	@Override
	public String toString() {
		return "AuthenticateResponse [user=" + user + ", authorizationToken=" + authorizationToken + ", code=" + code + ", description="
				+ description + "]";
	}	
	
}
