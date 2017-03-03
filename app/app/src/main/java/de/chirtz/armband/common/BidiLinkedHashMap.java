package de.chirtz.armband.common;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class BidiLinkedHashMap<K, V> implements Map<K, V> {

    private ArrayList<K> keys;
    private ArrayList<V> values;

    public BidiLinkedHashMap(int capacity) {
        keys = new ArrayList<>(capacity);
        values = new ArrayList<>(capacity);
    }

    public BidiLinkedHashMap() {
        keys = new ArrayList<>();
        values = new ArrayList<>();
    }

    @Override
    public void clear() {
        keys.clear();
        values().clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    public V getValueForKey(K key) {
        return get(key);
    }

    public K getKeyForValue(V val) {
        return keys.get(values.indexOf(val));
    }

    public V getLastValue() {
        return values.get(values.size()-1);
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new IllegalArgumentException("Not implemented");
    }

    @Override
    public V get(Object key) {
        return values.get(keys.indexOf(key));
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        Set<K> s = new HashSet<>();
        s.addAll(keys);
        return s;
    }

    @Override
    public V put(K key, V value) {
        keys.add(key);
        values.add(value);
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        keys.addAll(map.keySet());
        values.addAll(map.values());
    }

    @Override
    public V remove(Object key) {
        int pos = keys.indexOf(key);
        keys.remove(pos);
        return values.remove(pos);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return values;
    }

    @NonNull
    public List<V> valueList() {
        return values;
    }

    @NonNull
    public List<K> keyList() {
        return keys;
    }
}
