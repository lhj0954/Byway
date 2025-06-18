package com.example.byway;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RouteCardAdapter
        extends RecyclerView.Adapter<RouteCardAdapter.ViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(RouteInfo info);
    }

    private final List<RouteInfo> routes;
    private final OnRouteClickListener listener;

    public RouteCardAdapter(List<RouteInfo> routes, OnRouteClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    /** 새로운 리스트로 교체하고 화면 갱신 */
    public void updateData(List<RouteInfo> newRoutes) {
        routes.clear();
        routes.addAll(newRoutes);
        notifyDataSetChanged();
    }

    /** (선택) 완전 초기화 */
    public void clear() {
        routes.clear();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        RouteInfo info = routes.get(pos);
        // 시간 설정 (분 단위)
        holder.tvTime.setText(info.getDuration() + "분");
        // 거리 설정 (미터, 반올림)
        int meters = (int) Math.round(info.getDistance());
        holder.tvDistance.setText(meters + "m");

        holder.itemView.setOnClickListener(v -> listener.onRouteClick(info));
    }

    @Override public int getItemCount() {
        return routes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDistance;
        ViewHolder(View itemView) {
            super(itemView);
            tvTime     = itemView.findViewById(R.id.tv_route_time);
            tvDistance = itemView.findViewById(R.id.tv_route_distance);
        }
    }
}
