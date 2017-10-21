package com.elavon.converge.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.elavon.converge.R;

import co.poynt.os.model.Payment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckCardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckCardFragment extends DialogFragment {
    private static final String ARG_PAYMENT = "payment";
    private static final String ARG_CARDHOLDER_NAME = "cardHolderName";
    private static final String ARG_BIN_RANGE = "binRange";
    private static final String ARG_LAST_4 = "last4";
    private static final String ARG_AID = "aid";
    private static final String ARG_EXPIRATION = "expiration";
    private static final String ARG_SERVICECODE = "serviceCode";
    private static final String ARG_APPLICATION_LABEL = "applicationLabel";
    private static final String ARG_PAN_SEQUENCE_NUMBER = "panSequenceNumber";
    private static final String ARG_ISSUER_COUNTRY_CODE = "issuerCountryCode";
    private static final String ARG_ENCRYPTED_TRACK2 = "encryptedTrack2";
    private static final String ARG_ISSUER_CODE_TABLE_INDEX = "issuerCodeTableIndex";
    private static final String ARG_APPLICATION_PREF_NAME = "applicationPreferredName";
    private static final String ARG_KEY_IDENTIFIER = "keyIdentifier";
    private static final String ARG_ENCRYPTED_PAN = "encryptedPAN";
    private Payment payment;
    private String cardHolderName, seriviceCode, last4, binRange, aid, expiration;
    private String applicationLabel, issuerCountryCode, panSequenceNumber;
    private String encryptedPAN, encryptedTrack2, applicationPreferredName, keyIdentifier;
    private int issuerCodeTableIndex;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param payment              Payment.
     * @param serviceCode
     * @param holderName
     * @param last4
     * @param binRange
     * @param issuerCodeTableIndex @return A new instance of fragment ZipCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckCardFragment newInstance(Payment payment, String serviceCode, String holderName,
                                                String last4, String binRange, String expiration,
                                                String aid, String applicationLabel,
                                                String panSequenceNumber, String issuerCountryCode,
                                                String encryptedPAN,
                                                String encryptedTrack2, int issuerCodeTableIndex,
                                                String applicationPreferredName,
                                                String keyIdentifier) {

        CheckCardFragment fragment = new CheckCardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAYMENT, payment);
        args.putString(ARG_SERVICECODE, serviceCode);
        args.putString(ARG_CARDHOLDER_NAME, holderName);
        args.putString(ARG_LAST_4, last4);
        args.putString(ARG_BIN_RANGE, binRange);
        args.putString(ARG_EXPIRATION, expiration);
        args.putString(ARG_AID, aid);
        args.putString(ARG_APPLICATION_LABEL, applicationLabel);
        args.putString(ARG_PAN_SEQUENCE_NUMBER, panSequenceNumber);
        args.putString(ARG_ISSUER_COUNTRY_CODE, issuerCountryCode);
        args.putString(ARG_ENCRYPTED_PAN, encryptedPAN);
        args.putString(ARG_ENCRYPTED_TRACK2, encryptedTrack2);
        args.putInt(ARG_ISSUER_CODE_TABLE_INDEX, issuerCodeTableIndex);
        args.putString(ARG_APPLICATION_PREF_NAME, applicationPreferredName);
        args.putString(ARG_KEY_IDENTIFIER, keyIdentifier);
        fragment.setArguments(args);
        return fragment;
    }

    public CheckCardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            payment = getArguments().getParcelable(ARG_PAYMENT);
            cardHolderName = getArguments().getString(ARG_CARDHOLDER_NAME);
            seriviceCode = getArguments().getString(ARG_SERVICECODE);
            last4 = getArguments().getString(ARG_LAST_4);
            binRange = getArguments().getString(ARG_BIN_RANGE);
            aid = getArguments().getString(ARG_AID);
            expiration = getArguments().getString(ARG_EXPIRATION);
            applicationLabel = getArguments().getString(ARG_APPLICATION_LABEL);
            panSequenceNumber = getArguments().getString(ARG_PAN_SEQUENCE_NUMBER);
            issuerCountryCode = getArguments().getString(ARG_ISSUER_COUNTRY_CODE);
            encryptedPAN = getArguments().getString(ARG_ENCRYPTED_PAN);
            encryptedTrack2 = getArguments().getString(ARG_ENCRYPTED_TRACK2);
            issuerCodeTableIndex = getArguments().getInt(ARG_ISSUER_CODE_TABLE_INDEX);
            applicationPreferredName = getArguments().getString(ARG_APPLICATION_PREF_NAME);
            keyIdentifier = getArguments().getString(ARG_KEY_IDENTIFIER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_card, container, false);

        ((TextView) view.findViewById(R.id.cardHolderName)).setText(cardHolderName);
        ((TextView) view.findViewById(R.id.last4)).setText(last4);
        ((TextView) view.findViewById(R.id.binRange)).setText(binRange);
        ((TextView) view.findViewById(R.id.expiration)).setText(expiration);
        ((TextView) view.findViewById(R.id.aid)).setText(aid);
        ((TextView) view.findViewById(R.id.servicecode)).setText(seriviceCode);
        ((TextView) view.findViewById(R.id.applicationLabel)).setText(applicationLabel);
        ((TextView) view.findViewById(R.id.panSequenceNumber)).setText(panSequenceNumber);
        ((TextView) view.findViewById(R.id.issuerCountryCode)).setText(issuerCountryCode);
        ((TextView) view.findViewById(R.id.encryptedPAN)).setText(encryptedPAN);
        ((TextView) view.findViewById(R.id.encryptedTrack2)).setText(encryptedTrack2);
        ((TextView) view.findViewById(R.id.issuerCodeTableIndex)).setText(Integer.toString(issuerCodeTableIndex));
        ((TextView) view.findViewById(R.id.applicationPreferredName)).setText(applicationPreferredName);
        ((TextView) view.findViewById(R.id.keyIdentifier)).setText(keyIdentifier);


        Button enterButton = (Button) view.findViewById(R.id.submitButton);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onContinue(payment);
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancel();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onContinue(Payment payment);

        void onCancel();
    }

}
