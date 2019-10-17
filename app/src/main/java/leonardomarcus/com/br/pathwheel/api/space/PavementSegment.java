package leonardomarcus.com.br.pathwheel.api.space;

import java.util.List;

import leonardomarcus.com.br.pathwheel.api.model.PavementSample;
import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;

public class PavementSegment {
	private Double latitudeInit;
	private Double longitudeInit;
	private Double latitudeEnd;
	private Double longitudeEnd;
	private Double verticalAcceleration;
	private Double slopePercentage;
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
	public Double getVerticalAcceleration() {
		return verticalAcceleration;
	}
	public void setVerticalAcceleration(Double verticalAcceleration) {
		this.verticalAcceleration = verticalAcceleration;
	}
	public Double getSlopePercentage() {
		return slopePercentage;
	}
	public void setSlopePercentage(Double slopePercentage) {
		this.slopePercentage = slopePercentage;
	}
	
	
	public void setCoordinateInit(GeographicCoordinate coordinate) {
        this.latitudeInit = coordinate.getLatitude();
        this.longitudeInit = coordinate.getLongitude();
    }
    
    public void setCoordinateEnd(GeographicCoordinate coordinate) {
        this.latitudeEnd = coordinate.getLatitude();
        this.longitudeEnd = coordinate.getLongitude();
    }
    
    public void setVerticalAcceleration(List<PavementSample> pavementSamples) {
        double value = 0;
        for(PavementSample pavementSample : pavementSamples) {
            value+=pavementSample.getVerticalAcceleration();
        }
        if(pavementSamples.size() > 0) {
            value = value/pavementSamples.size();
        }
        this.verticalAcceleration = value;
    }
    
    public void setSlopePercentagem(Double previousElevation, double elevation, double distance) {
        this.slopePercentage = distance != 0 ? ((elevation - previousElevation) * 100) / distance : 0;
    }
	
	
	@Override
	public String toString() {
		return "PavementSegment [latitudeInit=" + latitudeInit + ", longitudeInit=" + longitudeInit + ", latitudeEnd="
				+ latitudeEnd + ", longitudeEnd=" + longitudeEnd + ", verticalAcceleration=" + verticalAcceleration
				+ ", slopePercentage=" + slopePercentage + "]";
	}
	
}
