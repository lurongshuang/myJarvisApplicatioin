package snow.music.activity.navigation.adapter;// Created byjinengmao

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import snow.music.R;
import snow.music.activity.navigation.bean.ChatMessage;

// on 2023/4/7
// Description：
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<ChatMessage> chatMessages = new ArrayList<>();

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        return chatMessage.getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 根据类型来决定加上什么holder
        if (viewType == ChatMessage.TYPE_RECEIVED) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_item, parent, false);
            return new leftHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_item, parent, false);
            return new rightHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        // 根据不同的holder来决定加载数据
        if (holder instanceof rightHolder) {
            ((rightHolder) holder).textView.setText(message.getMessage());
        }
        if (holder instanceof leftHolder) {
            ((leftHolder) holder).textView.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class leftHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public leftHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chat_left_text_view);
        }
    }

    static class rightHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public rightHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chat_right_text_view);
        }
    }
}
