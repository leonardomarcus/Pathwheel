package leonardomarcus.com.br.pathwheel.api.response;


import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.api.space.PavementSegment;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;

public class RouteMapResponse extends Response {
	List<GeographicCoordinate> coordinateSamples = new ArrayList<GeographicCoordinate>();
	List<PavementSegment> pavementSegments = new ArrayList<PavementSegment>();
	List<Spot> spots = new ArrayList<Spot>();
	double totalDistance = 0;
	double avgSpeed = 0;
	double estimatedTime = 0;
	public List<GeographicCoordinate> getCoordinateSamples() {
		return coordinateSamples;
	}
	public void setCoordinateSamples(List<GeographicCoordinate> coordinateSamples) {
		this.coordinateSamples = coordinateSamples;
	}
	public List<PavementSegment> getPavementSegments() {
		return pavementSegments;
	}
	public void setPavementSegments(List<PavementSegment> pavementSegments) {
		this.pavementSegments = pavementSegments;
	}
	public List<Spot> getSpots() {
		return spots;
	}
	public void setSpots(List<Spot> spots) {
		this.spots = spots;
	}
	public double getTotalDistance() {
		return totalDistance;
	}
	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}
	public double getAvgSpeed() {
		return avgSpeed;
	}
	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
	public double getEstimatedTime() {
		return estimatedTime;
	}
	public void setEstimatedTime(double estimatedTime) {
		this.estimatedTime = estimatedTime;
	}
	@Override
	public String toString() {
		return "RouteMapResponse [coordinateSamples=" + coordinateSamples + ", pavementSegments=" + pavementSegments
				+ ", spots=" + spots + ", totalDistance=" + totalDistance + ", avgSpeed=" + avgSpeed
				+ ", estimatedTime=" + estimatedTime + "]";
	}
	
	
}
