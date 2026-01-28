package com.example.islandlyrics;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhitelistActivity extends BaseActivity {

    private WhitelistAdapter adapter;
    private RecyclerView recyclerView;
    private List<WhitelistItem> packageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        // Fix Insets (Status Bar)
        View rootView = findViewById(R.id.root_view);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Load Data
        loadData();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WhitelistAdapter();
        recyclerView.setAdapter(adapter);

        // Add Button Logic
        findViewById(R.id.btn_add_package).setOnClickListener(v -> showAddDialog());
    }

    private void loadData() {
        packageList = WhitelistHelper.loadWhitelist(this);
    }

    private void saveData() {
        WhitelistHelper.saveWhitelist(this, packageList);
    }

    private void showAddDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.dialog_enter_pkg);
        
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 48;
        params.rightMargin = 48;
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_whitelist)
            .setView(container)
            .setPositiveButton(R.string.dialog_btn_add, (dialog, which) -> {
                String pkg = input.getText().toString().trim();
                if (!pkg.isEmpty()) {
                    boolean exists = false;
                    for (WhitelistItem item : packageList) {
                        if (item.getPackageName().equals(pkg)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (!exists) {
                        packageList.add(new WhitelistItem(pkg, true));
                        Collections.sort(packageList);
                        saveData();
                        adapter.notifyDataSetChanged();
                        AppLogger.getInstance().log("Whitelist", "Added: " + pkg);
                    } else {
                        Toast.makeText(this, "Package already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .show();
    }

    // Inner Adapter Class
    private class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_whitelist, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WhitelistItem item = packageList.get(position);
            holder.tvPackage.setText(item.getPackageName());
            
            // Unbind listener before setting state to avoid infinite triggers (rare but possible)
            holder.switchToggle.setOnCheckedChangeListener(null);
            holder.switchToggle.setChecked(item.isEnabled());
            
            holder.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setEnabled(isChecked);
                saveData();
            });

            // Long Press to Delete
            holder.itemView.setOnLongClickListener(v -> {
                int actualPos = holder.getAdapterPosition();
                if (actualPos != RecyclerView.NO_POSITION) {
                     new AlertDialog.Builder(WhitelistActivity.this)
                        .setTitle(R.string.dialog_whitelist_title) // Reusing title
                        .setMessage(getString(R.string.dialog_delete_confirm, item.getPackageName()))
                        .setPositiveButton(android.R.string.ok, (d, w) -> {
                            packageList.remove(actualPos);
                            saveData();
                            notifyItemRemoved(actualPos);
                            AppLogger.getInstance().log("Whitelist", "Removed: " + item.getPackageName());
                        })
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .show();
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPackage;
            com.google.android.material.materialswitch.MaterialSwitch switchToggle;

            ViewHolder(View itemView) {
                super(itemView);
                tvPackage = itemView.findViewById(R.id.tv_package_name);
                switchToggle = itemView.findViewById(R.id.switch_whitelist_toggle);
            }
        }
    }
}
