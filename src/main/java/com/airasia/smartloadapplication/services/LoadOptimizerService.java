package com.airasia.smartloadapplication.services;

import com.airasia.smartloadapplication.entities.OptimizeRequest;
import com.airasia.smartloadapplication.entities.OptimizeResponse;
import com.airasia.smartloadapplication.entities.Order;
import com.airasia.smartloadapplication.entities.Truck;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Service
public class LoadOptimizerService {

    private final Map<String, OptimizeResponse> cache = new ConcurrentHashMap<>();

    public OptimizeResponse optimize(OptimizeRequest request) {

        String key = cacheKey(request);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }


        for (Order o : request.getOrders()) {
            if (o.getPickupDate().isAfter(o.getDeliveryDate())) {
                throw new IllegalArgumentException("Invalid time window");
            }
        }

        OptimizeResult nonHazmat =
                solve(filter(request.getOrders(), false), request.getTruck());

        OptimizeResult hazmat =
                solve(filter(request.getOrders(), true), request.getTruck());

        OptimizeResult best =
                hazmat.totalPayout > nonHazmat.totalPayout ? hazmat : nonHazmat;

        OptimizeResponse response = buildResponse(request.getTruck(), best);
        cache.put(key, response);
        return response;
    }

    private List<Order> filter(List<Order> orders, boolean hazmat) {
        if (orders.isEmpty()) return orders;

        Order base = orders.get(0);
        return orders.stream()
                .filter(o -> o.isHazmat() == hazmat)
                .filter(o -> o.getOrigin().equals(base.getOrigin()))
                .filter(o -> o.getDestination().equals(base.getDestination()))
                .toList();
    }

    private OptimizeResult solve(List<Order> orders, Truck truck) {

        int n = orders.size();
        int maxMask = 1 << n;

        long[] w = new long[maxMask];
        long[] v = new long[maxMask];
        long[] p = new long[maxMask];
        boolean[] ok = new boolean[maxMask];

        ok[0] = true;

        long best = 0;
        int bestMask = 0;

        for (int mask = 1; mask < maxMask; mask++) {
            int bit = mask & -mask;
            int idx = Integer.numberOfTrailingZeros(bit);
            int prev = mask ^ bit;

            if (!ok[prev]) continue;

            Order o = orders.get(idx);

            w[mask] = w[prev] + o.getWeightLbs();
            v[mask] = v[prev] + o.getVolumeCuft();

            if (w[mask] > truck.getMaxWeightLbs()
                    || v[mask] > truck.getMaxVolumeCuft()) continue;

            p[mask] = p[prev] + o.getPayoutCents();
            ok[mask] = true;

            if (p[mask] > best) {
                best = p[mask];
                bestMask = mask;
            }
        }

        return new OptimizeResult(bestMask, best, w, v, orders);
    }

    private OptimizeResponse buildResponse(Truck truck, OptimizeResult r) {

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < r.orders.size(); i++) {
            if ((r.mask & (1 << i)) != 0) {
                ids.add(r.orders.get(i).getId());
            }
        }

        return OptimizeResponse.builder()
                .truckId(truck.getId())
                .selectedOrderIds(ids)
                .totalPayoutCents(r.totalPayout)
                .totalWeightLbs(r.weight[r.mask])
                .totalVolumeCuft(r.volume[r.mask])
                .utilizationWeightPercent(round(r.weight[r.mask] * 100.0 / truck.getMaxWeightLbs()))
                .utilizationVolumePercent(round(r.volume[r.mask] * 100.0 / truck.getMaxVolumeCuft()))
                .build();
    }

    private String cacheKey(OptimizeRequest request) {
        StringBuilder sb = new StringBuilder();
        Truck t = request.getTruck();

        sb.append(t.getId())
                .append(t.getMaxWeightLbs())
                .append(t.getMaxVolumeCuft());

        request.getOrders().stream()
                .sorted(Comparator.comparing(Order::getId))
                .forEach(o -> sb.append(o.getId())
                        .append(o.getPayoutCents())
                        .append(o.getWeightLbs())
                        .append(o.getVolumeCuft())
                        .append(o.isHazmat())
                        .append(o.getPickupDate())
                        .append(o.getDeliveryDate()));

        return Integer.toHexString(sb.toString().hashCode());
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private record OptimizeResult(
            int mask,
            long totalPayout,
            long[] weight,
            long[] volume,
            List<Order> orders
    ) {}
}

