package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashMap;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashSet;

import javax.annotation.Nullable;
import java.util.Arrays;

@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class VariableStorage implements ITempVariableStorage, IScopedVariableStorage, IForeignVariableStorage {
    private static final int TEMP_INIT_CAPACITY = 16;
    private static final int SCOPED_INIT_CAPACITY = 16;

    private Object[] stackFrame = new Object[TEMP_INIT_CAPACITY];
    private final PooledStringHashMap<VariableValueHolder> scopedMap = new PooledStringHashMap<>(SCOPED_INIT_CAPACITY);
    private PooledStringHashMap<VariableValueHolder> publicMap = new PooledStringHashMap<>();

    public VariableStorage() {
    }

    private void ensureStackFrameSize(int size) {
        if (this.stackFrame.length >= size) {
            return;
        }
        if (size < this.stackFrame.length * 2) {
            size = this.stackFrame.length * 2;
        }
        this.stackFrame = Arrays.copyOf(this.stackFrame, size);
    }

    @Override
    public Object getTemp(int index) {
        this.ensureStackFrameSize(index + 1);
        return this.stackFrame[index];
    }

    @Override
    public void setTemp(int index, Object value) {
        this.ensureStackFrameSize(index + 1);
        this.stackFrame[index] = value;
    }

    @Override
    public Object getScoped(int name) {
        VariableValueHolder valueHolder = this.scopedMap.computeIfAbsent(name, n -> new VariableValueHolder());
        return valueHolder.value;
    }

    @Override
    public void setScoped(int name, Object value) {
        VariableValueHolder valueHolder = this.scopedMap.computeIfAbsent(name, n -> new VariableValueHolder());
        valueHolder.value = value;
    }

    @Override
    public Object getPublic(int name) {
        VariableValueHolder valueHolder = this.publicMap.get(name);
        if (valueHolder != null) {
            return valueHolder.value;
        } else {
            return null;
        }
    }

    // 注意 this.publicMap 线程安全
    public void initialize(@Nullable PooledStringHashSet publicVariableNames) {
        Arrays.fill(this.stackFrame, null);
        this.scopedMap.clear();

        PooledStringHashMap<VariableValueHolder> newPublicMap = new PooledStringHashMap<>();
        if (publicVariableNames != null) {
            for (int publicVariableName : publicVariableNames) {
                VariableValueHolder value = new VariableValueHolder();
                this.scopedMap.put(publicVariableName, value);
                newPublicMap.put(publicVariableName, value);
            }
        }
        newPublicMap.trim();
        this.publicMap = newPublicMap;
    }

    private static class VariableValueHolder {
        public Object value = null;
    }
}
