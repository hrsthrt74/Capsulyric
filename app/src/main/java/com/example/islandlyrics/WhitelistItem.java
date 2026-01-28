package com.example.islandlyrics;

import java.util.Objects;

public class WhitelistItem implements Comparable<WhitelistItem> {
    private String packageName;
    private boolean isEnabled;

    public WhitelistItem(String packageName, boolean isEnabled) {
        this.packageName = packageName;
        this.isEnabled = isEnabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhitelistItem that = (WhitelistItem) o;
        return Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName);
    }

    @Override
    public int compareTo(WhitelistItem other) {
        return this.packageName.compareTo(other.packageName);
    }
}
