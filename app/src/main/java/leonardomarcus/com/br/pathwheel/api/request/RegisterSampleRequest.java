package leonardomarcus.com.br.pathwheel.api.request;

import leonardomarcus.com.br.pathwheel.api.model.PavementSample;

/**
 * Created by leonardo on 18/07/19.
 */

public class RegisterSampleRequest extends Request {
    private PavementSample sample;
    private String smartDevice;

    public PavementSample getSample() {
        return sample;
    }

    public void setSample(PavementSample sample) {
        this.sample = sample;
    }

    public String getSmartDevice() {
        return smartDevice;
    }

    public void setSmartDevice(String smartDevice) {
        this.smartDevice = smartDevice;
    }

    @Override
    public String toString() {
        return "RegisterPavementSampleRequest [sample=" + sample + ", smartDevice=" + smartDevice + "]";
    }



}