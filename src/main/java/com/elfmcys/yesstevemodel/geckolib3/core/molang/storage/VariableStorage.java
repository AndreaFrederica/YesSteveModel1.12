package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashMap;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashSet;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class VariableStorage implements ITempVariableStorage, IScopedVariableStorage, IForeignVariableStorage {
    private static final int TEMP_INIT_CAPACITY = 16;
    private static final int SCOPED_INIT_CAPACITY = 16;
    private static final int MAX_DEPTH = 32;

    private Object[] stackFrame = new Object[TEMP_INIT_CAPACITY];
    private int baseOffset;
    private int currentSize;
    private int scopeStart;
    private int scopeSize;
    private final ArrayDeque<long[]> scopeStack = new ArrayDeque<>();
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
        if (index < this.currentSize) {
            return this.stackFrame[this.baseOffset + index];
        }
        return null;
    }

    @Override
    public void setTemp(int index, Object value) {
        int size = index + 1;
        if (this.currentSize < size) {
            this.currentSize = size;
            this.ensureStackFrameSize(this.baseOffset + size);
        }
        this.stackFrame[this.baseOffset + index] = value;
    }

    public boolean pushScope(List<?> list) {
        if (this.scopeStack.size() >= MAX_DEPTH) {
            return false;
        }
        int offset = this.baseOffset + this.currentSize;
        int size = list.size();
        this.ensureStackFrameSize(offset + size);
        for (int i = 0; i < size; i++) {
            this.stackFrame[offset + i] = list.get(i);
        }
        this.scopeStack.addLast(new long[]{this.scopeStart, this.scopeSize});
        this.scopeStart = offset;
        this.scopeSize = size;
        this.baseOffset = offset + size;
        this.currentSize = 0;
        return true;
    }

    public boolean pushScopeWithArgs(ExecutionContext<?> context, Function.ArgumentCollection arguments) {
        if (this.scopeStack.size() >= MAX_DEPTH) {
            return false;
        }
        int offset = this.baseOffset + this.currentSize;
        int size = arguments.size();
        this.ensureStackFrameSize(offset + size);
        for (int i = 0; i < size; i++) {
            this.stackFrame[offset + i] = arguments.getValue(context, i);
        }
        this.scopeStack.addLast(new long[]{this.scopeStart, this.scopeSize});
        this.scopeStart = offset;
        this.scopeSize = size;
        this.baseOffset = offset + size;
        this.currentSize = 0;
        return true;
    }

    public void popScope() {
        if (this.scopeStack.isEmpty()) {
            return;
        }
        int previousScopeStart = this.scopeStart;
        long[] previousScope = this.scopeStack.removeLast();
        this.scopeStart = (int) previousScope[0];
        this.scopeSize = (int) previousScope[1];
        this.baseOffset = this.scopeStart + this.scopeSize;
        this.currentSize = previousScopeStart - this.baseOffset;
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
        this.baseOffset = 0;
        this.currentSize = 0;
        this.scopeStart = 0;
        this.scopeSize = 0;
        this.scopeStack.clear();
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
