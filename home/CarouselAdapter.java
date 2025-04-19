package com.example.summer.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.summer.R;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
    static final int LOOP_MULTIPLIER = 500; // 增加循环倍数以确保更平滑的无限滚动
    private final List<Integer> imageResources;
    private final RequestOptions glideOptions;
    private final AtomicBoolean isPreloadActive = new AtomicBoolean(true);

    public CarouselAdapter(List<Integer> images) {
        this.imageResources = images;
        
        // 预先创建Glide配置，提高性能
        this.glideOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(800, 600)
                .centerCrop()
                .dontAnimate();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 为每个视图创建一个新的 ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carousel_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return imageResources.isEmpty() ? 0 : imageResources.size() * LOOP_MULTIPLIER;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int realPos = getRealPosition(position);
        Context context = holder.itemView.getContext();
        
        // 使用高效的Glide配置加载图片
        Glide.with(context)
                .load(imageResources.get(realPos))
                .apply(glideOptions)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(holder.imageView);
                
        // 预加载下一张图片，提高滑动流畅性
        if (isPreloadActive.get()) {
            preloadNextImage(context, realPos);
        }
    }
    
    private void preloadNextImage(Context context, int currentRealPosition) {
        int nextPosition = (currentRealPosition + 1) % getRealCount();
        Glide.with(context)
                .load(imageResources.get(nextPosition))
                .apply(glideOptions)
                .preload();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            
            // 优化ImageView性能
            imageView.setHasTransientState(true);
        }
    }

    public int getRealCount() {
        return imageResources.size();
    }

    // 计算实际位置
    public int getRealPosition(int position) {
        return position % getRealCount();
    }
    
    // 禁用预加载（当应用在后台或不可见时调用）
    public void disablePreloading() {
        isPreloadActive.set(false);
    }
    
    // 启用预加载
    public void enablePreloading() {
        isPreloadActive.set(true);
    }
    
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // 取消未完成的加载任务，释放资源
        Glide.with(holder.itemView.getContext()).clear(holder.imageView);
        holder.imageView.setImageDrawable(null);
    }
}

// CarouselAdapter.java
