package com.airasia.smartloadapplication.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Order {

    private String id;

    @JsonProperty("payout_cents")
    private long payoutCents;

    @JsonProperty("weight_lbs")
    private long weightLbs;

    @JsonProperty("volume_cuft")
    private long volumeCuft;

    private String origin;
    private String destination;

    @JsonProperty("pickup_date")
    private LocalDate pickupDate;

    @JsonProperty("delivery_date")
    private LocalDate deliveryDate;

    @JsonProperty("is_hazmat")
    private boolean hazmat;
}

