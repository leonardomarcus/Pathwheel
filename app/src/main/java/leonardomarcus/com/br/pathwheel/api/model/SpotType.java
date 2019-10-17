package leonardomarcus.com.br.pathwheel.api.model;

public class SpotType {
	public static final Integer ALERT = 1;
	public static final Integer DANGER = 2;
	public static final Integer BARRIER = 3;
	
	private Integer id;
	private String description;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "SpotType [id=" + id + ", description=" + description + "]";
	}
	
}
