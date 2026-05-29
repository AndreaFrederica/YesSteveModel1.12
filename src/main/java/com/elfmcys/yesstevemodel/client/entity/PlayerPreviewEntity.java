package com.elfmcys.yesstevemodel.client.entity;

import org.apache.commons.lang3.StringUtils;

public class PlayerPreviewEntity extends CustomPlayerEntity implements IPreviewAnimatable {

    private final AnimationStateMachine animationStateMachine = new AnimationStateMachine();
    private boolean customAnimationActive;

    public PlayerPreviewEntity() {
        super(null);
    }

    @Override
    public void setPreviewAnimation(String previewAnimation) {
        super.setPreviewAnimation(previewAnimation);
        this.animationStateMachine.setCurrentAnimation(previewAnimation);
    }

    @Override
    public void clearPreviewAnimation() {
        super.clearPreviewAnimation();
        this.animationStateMachine.setCurrentAnimation(StringUtils.EMPTY);
        this.animationStateMachine.setQueuedAnimation(StringUtils.EMPTY);
        this.animationStateMachine.setPreviousAnimation(StringUtils.EMPTY);
        this.customAnimationActive = false;
    }

    @Override
    public boolean hasPreviewAnimation() {
        return this.animationStateMachine.hasAnimation();
    }

    @Override
    public boolean hasPreviewAnimation(String previewAnimation) {
        return StringUtils.equals(this.animationStateMachine.getCurrentAnimation(), previewAnimation);
    }

    @Override
    public void setCustomAnimationActive(boolean active) {
        this.customAnimationActive = active;
    }

    public boolean isCustomAnimationActive() {
        return this.customAnimationActive;
    }

    @Override
    public AnimationStateMachine getAnimationStateMachine() {
        return this.animationStateMachine;
    }
}
