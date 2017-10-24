package com.axzae.homeassistant.fragment.control;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.axzae.homeassistant.R;
import com.axzae.homeassistant.model.Entity;
import com.axzae.homeassistant.model.rest.CallServiceRequest;
import com.axzae.homeassistant.util.CommonUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InputSliderFragment extends BaseControlFragment implements View.OnClickListener {

    private DiscreteSeekBar mSeekBar;

    public static InputSliderFragment newInstance(Entity entity) {
        InputSliderFragment fragment = new InputSliderFragment();
        Bundle args = new Bundle();
        args.putString("entity", CommonUtil.deflate(entity));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.control_input_slider, null);
        builder.setView(rootView);
        builder.setTitle(mEntity.getFriendlyName());

        rootView.findViewById(R.id.button_cancel).setOnClickListener(this);
        rootView.findViewById(R.id.button_set).setOnClickListener(this);

        mSeekBar = rootView.findViewById(R.id.discrete_data);
        final TextView valueView = rootView.findViewById(R.id.text_data);

        DiscreteSeekBar.NumericTransformer multiplyTransformer =
                new DiscreteSeekBar.NumericTransformer() {
                    @Override
                    public int transform(int value) {
                        return value; // value * mEntity.attributes.step;
                    }

                    @Override
                    public String transformToString(int value) {
                        BigDecimal finalValue = new BigDecimal(value).multiply(mEntity.attributes.step).add(mEntity.attributes.min);
                        return String.format(Locale.ENGLISH, "%." + mEntity.attributes.getNumberOfDecimalPlaces() + "f", finalValue);
                    }

                    @Override
                    public boolean useStringTransform() {
                        return true;
                    }
                };


        mSeekBar.setNumericTransformer(multiplyTransformer);
        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                BigDecimal finalValue = new BigDecimal(seekBar.getProgress()).multiply(mEntity.attributes.step).add(mEntity.attributes.min);
                valueView.setText(String.format(Locale.ENGLISH, "%." + mEntity.attributes.getNumberOfDecimalPlaces() + "f", finalValue));
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        mSeekBar.setMin(0);
        mSeekBar.setMax(mEntity.attributes.max.subtract(mEntity.attributes.min).divide(mEntity.attributes.step, RoundingMode.HALF_UP).intValue());
        mSeekBar.setProgress(new BigDecimal(mEntity.state).subtract(mEntity.attributes.min).divide(mEntity.attributes.step, RoundingMode.HALF_UP).intValue());

        return builder.create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_set:
                BigDecimal finalValue = new BigDecimal(mSeekBar.getProgress()).multiply(mEntity.attributes.step).add(mEntity.attributes.min);
                callService(mEntity.getDomain(), "select_value", new CallServiceRequest(mEntity.entityId).setValue(String.format(Locale.ENGLISH, "%." + mEntity.attributes.getNumberOfDecimalPlaces() + "f", finalValue)));
                dismiss();
        }
    }
}
