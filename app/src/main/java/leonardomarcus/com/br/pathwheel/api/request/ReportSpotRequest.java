package leonardomarcus.com.br.pathwheel.api.request;

public class ReportSpotRequest extends Request {
    private long spotId;
    private long userId;
    private int spotReportTypeId;

    public long getSpotId() {
        return spotId;
    }

    public void setSpotId(long spotId) {
        this.spotId = spotId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getSpotReportTypeId() {
        return spotReportTypeId;
    }

    public void setSpotReportTypeId(int spotReportTypeId) {
        this.spotReportTypeId = spotReportTypeId;
    }

    @Override
    public String toString() {
        return "ReportSpotRequest{" +
                "spotId=" + spotId +
                ", userId=" + userId +
                ", spotReportTypeId=" + spotReportTypeId +
                '}';
    }
}
