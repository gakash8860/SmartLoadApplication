package com.airasia.smartloadapplication.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Truck {

    private String id;

    @JsonProperty("max_weight_lbs")
    private long maxWeightLbs;

    @JsonProperty("max_volume_cuft")
    private long maxVolumeCuft;
}
