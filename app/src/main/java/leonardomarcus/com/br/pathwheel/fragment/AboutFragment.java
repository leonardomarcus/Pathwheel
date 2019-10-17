package leonardomarcus.com.br.pathwheel.fragment;

import android.app.Fragment;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

public class AboutFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.getInstance().setPathwheelTitle("Sobre...");

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        try {
            PackageInfo pInfo = rootView.getContext().getPackageManager().getPackageInfo(rootView.getContext().getPackageName(), 0);
            String version = pInfo.versionName;
            //int verCode = pInfo.versionCode;

            TextView textViewVersion = (TextView) rootView.findViewById(R.id.textView_about_version);
            textViewVersion.setText("v"+version);
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(rootView.getContext(),"get version failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

}
