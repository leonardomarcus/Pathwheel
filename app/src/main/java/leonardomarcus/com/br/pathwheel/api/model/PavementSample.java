package leonardomarcus.com.br.pathwheel.api.model;

import java.util.ArrayList;
import java.util.List;

public class PavementSample {
	private Long id;
	private String registrationDate;
	private Double latitudeInit;
	private Double longitudeInit;
	private Double latitudeEnd;
	private Double longitudeEnd;
	private Double elapsedTime;
	private Double verticalAcceleration;
	private Double speed;
	private Double distance;
	private Double accuracy;
	private User user;
	private List<Double> verticalAccelerations = new ArrayList<>();
	private Integer steps;
	private Integer travelModeId;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	public Double getLatitudeInit() {
		return latitudeInit;
	}
	public void setLatitudeInit(Double latitudeInit) {
		this.latitudeInit = latitudeInit;
	}
	public Double getLongitudeInit() {
		return longitudeInit;
	}
	public void setLongitudeInit(Double longitudeInit) {
		this.longitudeInit = longitudeInit;
	}
	public Double getLatitudeEnd() {
		return latitudeEnd;
	}
	public void setLatitudeEnd(Double latitudeEnd) {
		this.latitudeEnd = latitudeEnd;
	}
	public Double getLongitudeEnd() {
		return longitudeEnd;
	}
	public void setLongitudeEnd(Double longitudeEnd) {
		this.longitudeEnd = longitudeEnd;
	}
	public Double getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(Double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	public Double getVerticalAcceleration() {
		return verticalAcceleration;
	}
	public void setVerticalAcceleration(Double verticalAcceleration) {
		this.verticalAcceleration = verticalAcceleration;
	}
	public Double getSpeed() {
		return speed;
	}
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	}
	public Double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public List<Double> getVerticalAccelerations() {
		return verticalAccelerations;
	}

	public void setVerticalAccelerations(List<Double> verticalAccelerations) {
		this.verticalAccelerations = verticalAccelerations;
	}

	public Integer getSteps() {
		return steps;
	}

	public void setSteps(Integer steps) {
		this.steps = steps;
	}

	public Integer getTravelModeId() {
		return travelModeId;
	}

	public void setTravelModeId(Integer travelModeId) {
		this.travelModeId = travelModeId;
	}

	@Override
	public String toString() {
		return "PavementSample [id=" + id + ", registrationDate=" + registrationDate + ", latitudeInit=" + latitudeInit
				+ ", longitudeInit=" + longitudeInit + ", latitudeEnd=" + latitudeEnd + ", longitudeEnd=" + longitudeEnd
				+ ", elapsedTime=" + elapsedTime + ", verticalAcceleration=" + verticalAcceleration + ", speed=" + speed
				+ ", distance=" + distance + ", accuracy=" + accuracy + ", user=" + user + ", verticalAccelerations="+verticalAccelerations.size()+", steps="+steps+", travelModeId="+travelModeId+"]";
	}	
}
