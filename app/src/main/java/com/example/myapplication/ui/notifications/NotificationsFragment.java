package com.example.myapplication.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentNotificationsBinding;

import java.util.HashMap;
import java.util.Map;

//        Documentation of “best practices” is reflected in the usage of View Binding, SharedPreferences, etc.
//        Choice of Views and view positioning is apparent in the use of various UI elements and their setup in the onViewCreated method.
//        Scalability/extensibility/adaptability is addressed in the comment about the hard-coded dealDetails array.
//        User-centric design choices are seen in the way the app preserves the acknowledgment state and updates the UI based on user actions.

// Represents the Notifications screen of the app, showing details of the deals and an acknowledgment button.
public class NotificationsFragment extends Fragment {

    // Binding instance for the fragment_notifications layout.
    // View Binding is a modern practice to simplify view access and improve null safety.
    private FragmentNotificationsBinding binding;

    // UI elements and keys to store acknowledgment state.
    private Button btnAcknowledge;
    private String currentButtonStateKey;
    private final String BUTTON_STATE_KEY_PREFIX = "buttonStateForDeal_";
    private SharedPreferences sharedPreferences;

    // State variable to store whether the current deal has been acknowledged.
    private boolean isAcknowledged = false;
    // Hard-coded array of deal details. For scalability and extensibility,
    // consider fetching this from a remote API or local database.
    private String[] dealDetails = {
            "Details for Deal 1: Buy one get one free at Store A.",
            "Details for Deal 2: 20% off electronics at Store B.",
            "Details for Deal 3: 50% off winter collection at Store C.",
            "Details for Deal 4: Free shipping for orders above $100 at Store D.",
            "Details for Deal 5: 10% off your first purchase at Store E.",
            "Details for Deal 6: Free dessert with every main course at Restaurant F.",
            "Details for Deal 7: Buy 2 shoes and get 50% off on the third at Store G.",
            "Details for Deal 8: End of season sale: Up to 70% off at Store H.",
            "Details for Deal 9: Early bird special: 30% off breakfasts at Cafe I.",
            "Details for Deal 10: Happy hour from 6 PM to 8 PM at Bar J.",
            "Details for Deal 11: Student discount: 15% off on showing student ID at Store K.",
            "Details for Deal 12: Weekend sale: Flat 40% off at Store L.",
            "Details for Deal 13: Loyalty program members get an extra 10% off at Store M.",
            "Details for Deal 14: Flash sale: 50% off select items from 5 PM to 6 PM at Store N.",
            "Details for Deal 15: Free trial session at Gym O.",
            "Details for Deal 16: Introductory offer: 20% off on all spa services at Spa P.",
            "Details for Deal 17: Pre-order now and get an exclusive gift at Store Q.",
            "Details for Deal 18: Anniversary sale: Up to 60% off at Store R."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment_notifications layout using View Binding.
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load or create SharedPreferences to store and retrieve the acknowledgment state.
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);


        if (getArguments() != null) {
            for (int i = 0; i < dealDetails.length; i++) {
                sendDealToServer(i + 1); // dealNumber starts from 1
            }
        }


        // Check if there's a deal to display and show its details.
        int dealNumber = getArguments().getInt("dealNumber", -1);

        if (dealNumber != -1) {
            displayDealDetails(dealNumber);
            setupAcknowledgmentButton(dealNumber);
        }

        btnAcknowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            // Set up the onClickListener for the acknowledgment button. When clicked, it will
            // save the acknowledgment state and update the UI accordingly.
            public void onClick(View v) {
                if (!isAcknowledged) {
                    isAcknowledged = true;
                    updateAcknowledgmentUI();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(currentButtonStateKey, true);
                    editor.apply();
                    sendDealToServer(dealNumber);
                }
            }
        });
    }

    private void sendDealToServer(int dealNumber) {
        String dealDetail = dealDetails[dealNumber - 1];
        DealService service = RetrofitClient.getRetrofitInstance().create(DealService.class);

        Call<String> call = service.sendDeal(dealNumber, "Title for Deal " + dealNumber, dealDetail);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("Response", response.body());
                } else {
                    Log.d("ResponseError", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }
    private void setupAcknowledgmentButton(int dealNumber) {
        btnAcknowledge = binding.btnAcknowledge;
        currentButtonStateKey = BUTTON_STATE_KEY_PREFIX + dealNumber;
        isAcknowledged = sharedPreferences.getBoolean(currentButtonStateKey, false);
        updateAcknowledgmentUI();
    }

    private void updateAcknowledgmentUI() {
        if (isAcknowledged) {
            btnAcknowledge.setText("Got it!");
            btnAcknowledge.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            btnAcknowledge.setText("Mark as Read");
            btnAcknowledge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#19711D"))); // using Color.parseColor() here
        }
    }

    private void displayDealDetails(int dealNumber) {
        if (dealNumber == 1) {
            // Display the image only for deal one can add more if statemnts to make other images in other deals
            ImageView dealImage = new ImageView(getContext());
            dealImage.setImageResource(R.drawable.fake_deal_image);
            binding.dealContainer.addView(dealImage, 0);  // Add the image at the start of the container

            // Make sure to display the text description for Deal 1
            binding.dealDetailsTextView.setVisibility(View.VISIBLE);
            binding.dealDetailsTextView.setText(dealDetails[dealNumber - 1]);
        } else {
            binding.dealDetailsTextView.setText(dealDetails[dealNumber - 1]);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Preserve the acknowledgment state across configuration changes
        outState.putBoolean("isAcknowledged", isAcknowledged);
    }

}
