package leonardomarcus.com.br.pathwheel.api.model;

public class TravelMode {

    public static final int WALKING = 0;
    public static final int WHEELCHAIR = 1;
    public static final int CAR = 2;
    public static final int BIKE = 3;

    private int id;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
        return "TravelMode{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}
