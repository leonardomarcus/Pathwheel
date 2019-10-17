package leonardomarcus.com.br.pathwheel.api.model;

public class SpotReportType {
    public static final int STILL_THERE = 1;
    public static final int NOT_THERE = 2;

    public SpotReportType(){}

    public SpotReportType(int id){
        this.id = id;
    }

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
        return "SpotReportType{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }
}
