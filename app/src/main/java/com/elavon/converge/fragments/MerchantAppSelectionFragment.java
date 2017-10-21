package com.elavon.converge.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.elavon.converge.R;

import java.util.ArrayList;
import java.util.List;

import co.poynt.os.model.EMVApplication;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAppSelectionListener} interface
 * to handle interaction events.
 * Use the {@link MerchantAppSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MerchantAppSelectionFragment extends DialogFragment {

    private List<EMVApplication> applicationItemList;
    private OnAppSelectionListener mListener;
    ListView applicationList;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppSelectionFragment.
     */
    public static MerchantAppSelectionFragment newInstance() {
        MerchantAppSelectionFragment fragment = new MerchantAppSelectionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MerchantAppSelectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.poynt_fragment_app_selection, container, false);

        applicationList = (ListView) view.findViewById(R.id.applicationList);
        applicationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EMVApplication selectedItem = applicationItemList.get(position);
                if (mListener != null) {
                    mListener.onApplicationSelected(selectedItem.getIndex());
                }
            }
        });

        loadApplications();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            super.onAttach(activity);
            mListener = (OnAppSelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMerchantAppSelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void loadApplications() {
        //if applistselector is ready - otherwise this will be called from onCreateview
        if (applicationList != null) {
            if (applicationItemList != null && applicationItemList.size() > 0) {
                List<String> appLabels = new ArrayList<>();
                for (EMVApplication item : applicationItemList) {

                    /**
                     The Application Preferred Name (9f12), if present and if the Issuer Code Table
                     Index indicating the part of ISO/IEC 8859 to use is present and supported by
                     the terminal (as indicated in Additional Terminal Capabilities Byte 5)
                     Otherwise the Application Label, if present, by using the common character set
                     of ISO/IEC 8859 (see Book 4 Annex B)
                     Failing both of those options you should use the AID value as a very last resort
                     as there is a test case that also tests what happens when the application label
                     and application preferred name are not present on the card.
                     **/

                    boolean displayPreferredName = false;

                    // if the bit is set display preferred NAme
                    if (item.getTerminalCapabilities() != null && item.getCodeTableIndex() > 0) {

                        // byte 4 bit 2: Code Table 10
                        // byte 4 bit 1: Code Table 9
                        // byte 5 bit 8: Code Table 8
                        // byte 5 bit 7: Code Table 7
                        // byte 5 bit 6: Code Table 6
                        // byte 5 bit 5: Code Table 5
                        // byte 5 bit 4: Code Table 4
                        // byte 5 bit 3: Code Table 3
                        // byte 5 bit 2: Code Table 2
                        // byte 5 bit 1: Code Table 1
                        switch (item.getCodeTableIndex()) {
                            case 1:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x01);
                                break;
                            case 2:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x02);
                                break;
                            case 3:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x04);
                                break;
                            case 4:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x08);
                                break;
                            case 5:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x10);
                                break;
                            case 6:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x20);
                                break;
                            case 7:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x40);
                                break;
                            case 8:
                                displayPreferredName = (item.getTerminalCapabilities()[4] == (byte) 0x80);
                                break;
                            case 9:
                                displayPreferredName = (item.getTerminalCapabilities()[3] == (byte) 0x01);
                                break;
                            case 10:
                                displayPreferredName = (item.getTerminalCapabilities()[3] == (byte) 0x02);
                                break;
                            default:
                                displayPreferredName = false;
                                break;
                        }
                    }
                    // priority should be preferred name followed by app label followed by AID
                    if (displayPreferredName
                            && item.getPreferredName() != null) {
                        appLabels.add(item.getPreferredName());
                    } else if (item.getLabel() != null) {
                        appLabels.add(item.getLabel());
                    } else if (item.getCardAID() != null) {
                        appLabels.add(item.getCardAID());
                    } else if (item.getDfName() != null) {
                        appLabels.add(item.getDfName());
                    } else {
                        appLabels.add("-name-not-available-");
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getActivity(), R.layout.poynt_fragment_app_selection_item, appLabels);
                applicationList.setAdapter(adapter);
            }
        }
    }

    public List<EMVApplication> getApplicationItemList() {
        return applicationItemList;
    }

    public void setApplicationItemList(List<EMVApplication> applicationItemList) {
        this.applicationItemList = applicationItemList;
        // refresh list
        loadApplications();
    }

    public interface OnAppSelectionListener {
        void onApplicationSelected(int index);

        void onCancelSelection();
    }
}
