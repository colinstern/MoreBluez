package sensors.morebluez;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

/**
 * Created by cost on 7/22/16.
 */
public class BluetoothFragment extends Fragment {

    //Request codes
    public static final int REQUEST_ENABLE_BT = 3;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothService mBtService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

    }

    public void onStart(Bundle savedInstanceState) {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

//         TODO add logic to handle BTService not being started
    }

    public void onResume(Bundle savedInstanceState) {
        super.onResume();
        if (mBtService != null) {
            //We need to start the service
            if (mBtService.getState() == BluetoothService.STATE_NONE) {
                mBtService.start();
            }
        }
    }

}
