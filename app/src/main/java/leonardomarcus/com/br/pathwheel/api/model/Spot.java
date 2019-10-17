package leonardomarcus.com.br.pathwheel.api.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.SpotDialogActivity;

public class Spot {
	private Long id;
	private SpotType spotType = new SpotType();
	private String registrationDate;
	private Double latitude;
	private Double longitude;
	private String comment;
	private User user = new User();
	private String picture;
	private boolean hasPicture;
	private int countStillThere;
	private int countNotThere;
	private Integer travelModeId = new Integer(1);

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public SpotType getSpotType() {
		return spotType;
	}
	public void setSpotType(SpotType spotType) {
		this.spotType = spotType;
	}
	public String getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public boolean isHasPicture() {
		return hasPicture;
	}

	public void setHasPicture(boolean hasPicture) {
		this.hasPicture = hasPicture;
	}

	public int getCountStillThere() {
		return countStillThere;
	}

	public void setCountStillThere(int countStillThere) {
		this.countStillThere = countStillThere;
	}

	public int getCountNotThere() {
		return countNotThere;
	}

	public void setCountNotThere(int countNotThere) {
		this.countNotThere = countNotThere;
	}

	public Integer getTravelModeId() {
		return travelModeId;
	}

	public void setTravelModeId(Integer travelModeId) {
		this.travelModeId = travelModeId;
	}

	@Override
	public String toString() {
		return "Spot [id=" + id + ", spotType=" + spotType + ", registrationDate=" + registrationDate + ", latitude="
				+ latitude + ", longitude=" + longitude + ", comment=" + comment + ", user=" + user + ", travelModeId="+travelModeId+"]";
	}

	public static List<Marker> addMarkers(final GoogleMap map, List<Spot> spots, final Context context) {
		List<Marker> markers = new ArrayList<>();
		for(final Spot spot : spots) {
			int resource = 1;
			String title = "ALERTA";
			if(spot.getSpotType().getId() == SpotType.ALERT) {
				resource = R.mipmap.ic_marker_alert;
				title = "ALERTA";
			}
			else if(spot.getSpotType().getId() == SpotType.DANGER) {
				resource = R.mipmap.ic_marker_danger;
				title = "PERIGO";
			}
			else if(spot.getSpotType().getId() == SpotType.BARRIER) {
				resource = R.mipmap.ic_marker_blocked;
				title = "BLOQUEIO";
			}
			Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(spot.getLatitude(),spot.getLongitude()))
					.title(title)
					.snippet(spot.getComment())
					.icon(BitmapDescriptorFactory.fromResource(resource))
			);
			marker.setTag(spot);
			map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {
					if(marker.getTag()!= null && marker.getTag() instanceof Spot) {
						Log.d("spot debug", ((Spot) marker.getTag()).toString());
						map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(((Spot) marker.getTag()).getLatitude(),((Spot) marker.getTag()).getLongitude())));
						PathwheelPreferences.setSpot(context,((Spot) marker.getTag()));

						Intent intent = new Intent(context, SpotDialogActivity.class);
						context.startActivity(intent);
						return true;
					}
					else {
						return false;
					}
				}
			});
			markers.add(marker);

		}
		return markers;
	}

	public static List<org.osmdroid.views.overlay.Marker> addOsmMarkers(final MapView map, List<Spot> spots, final Context context) {
		List<org.osmdroid.views.overlay.Marker> markers = new ArrayList<>();
		for(final Spot spot : spots) {
			int resource = 1;
			String title = "ALERTA";
			if(spot.getSpotType().getId() == SpotType.ALERT) {
				resource = R.mipmap.ic_marker_alert;
				title = "ALERTA";
			}
			else if(spot.getSpotType().getId() == SpotType.DANGER) {
				resource = R.mipmap.ic_marker_danger;
				title = "PERIGO";
			}
			else if(spot.getSpotType().getId() == SpotType.BARRIER) {
				resource = R.mipmap.ic_marker_blocked;
				title = "BLOQUEIO";
			}

			org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(map);
			marker.setPosition(new GeoPoint(spot.getLatitude(),spot.getLongitude()));
			marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
			marker.setIcon(context.getResources().getDrawable(resource));
			marker.setTitle(title);
			marker.setRelatedObject(spot);
			marker.setOnMarkerClickListener(new org.osmdroid.views.overlay.Marker.OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(org.osmdroid.views.overlay.Marker marker, MapView mapView) {
					if(marker.getRelatedObject()!= null && marker.getRelatedObject() instanceof Spot) {
						map.getController().setCenter(new GeoPoint(spot.getLatitude(),spot.getLongitude()));
						PathwheelPreferences.setSpot(context,((Spot) marker.getRelatedObject()));

						Intent intent = new Intent(context, SpotDialogActivity.class);
						context.startActivity(intent);
						return true;
					}
					else {
						return false;
					}
				}
			});
			markers.add(marker);
			map.getOverlays().add(marker);

		}
		return markers;
	}
	
}
