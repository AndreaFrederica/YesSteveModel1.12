package com.elfmcys.yesstevemodel.geckolib3.core.event;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import java.util.List;

public class InstructionKeyFrameExecutor {
    private final List<EventKeyFrame<IValue[]>> list;
    private int nextIndex = 0;

    public InstructionKeyFrameExecutor(List<EventKeyFrame<IValue[]>> list) {
        this.list = list;
    }

    private void evalValues(ExpressionEvaluator<?> evaluator, IValue[] values) {
        for (IValue value : values) {
            value.evalAsDouble(evaluator);
        }
    }

    public void executeTo(ExpressionEvaluator<?> evaluator, double currentTick) {
        while (!this.reachEnd()) {
            EventKeyFrame<IValue[]> keyFrame = this.list.get(this.nextIndex);
            if (keyFrame.getStartTick() > currentTick) {
                return;
            }
            this.evalValues(evaluator, keyFrame.getEventData());
            this.nextIndex++;
        }
    }

    public void executeRemaining(ExpressionEvaluator<?> evaluator) {
        for (int i = this.nextIndex; i < this.list.size(); i++) {
            this.evalValues(evaluator, this.list.get(i).getEventData());
        }
        this.nextIndex = this.list.size();
    }

    public boolean reachEnd() {
        return this.nextIndex >= this.list.size();
    }

    public void reset() {
        this.nextIndex = 0;
    }
}
