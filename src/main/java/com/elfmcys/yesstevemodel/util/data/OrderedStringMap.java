package com.elfmcys.yesstevemodel.util.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderedStringMap<K, V> implements Map<K, V> {
    private final List<K> keys;
    private final List<V> valuesList;
    private final Map<K, V> map;

    public OrderedStringMap(K[] keyArray, V[] valueArray) {
        this.keys = Collections.unmodifiableList(Arrays.asList(keyArray));
        this.valuesList = Collections.unmodifiableList(Arrays.asList(valueArray));
        Map<K, V> ordered = new LinkedHashMap<>();
        int size = Math.min(keyArray.length, valueArray.length);
        for (int index = 0; index < size; index++) {
            ordered.put(keyArray[index], valueArray[index]);
        }
        this.map = Collections.unmodifiableMap(ordered);
    }

    public K getKeyAt(int index) {
        return this.keys.get(index);
    }

    public List<K> getKeys() {
        return this.keys;
    }

    public V getValueAt(int index) {
        return this.valuesList.get(index);
    }

    public List<V> getValuesList() {
        return this.valuesList;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nonnull
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    @Nonnull
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    @Nonnull
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(this.map.entrySet());
    }
}